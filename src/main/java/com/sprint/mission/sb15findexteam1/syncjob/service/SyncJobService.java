package com.sprint.mission.sb15findexteam1.syncjob.service;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataService;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.service.IndexInfoService;
import com.sprint.mission.sb15findexteam1.openapi.client.OpenApiClient;
import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto.Item;
import com.sprint.mission.sb15findexteam1.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import com.sprint.mission.sb15findexteam1.syncjob.repository.SyncJobRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Open API 지수 정보를 IndexInfo·IndexData로 연동하고 항목별 SyncJob 이력을 남긴다.
 *
 * <p>한 항목 처리에 실패해도 전체 연동은 계속하며,
 * 실패한 항목만 FAILED 이력으로 기록한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobService {
  private static final DateTimeFormatter KRX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OpenApiClient openApiClient;
  private final SyncJobRepository syncJobRepository;
  private final IndexInfoService indexInfoService;
  private final IndexDataService indexDataService;

  /**
   * 최근 7일 데이터를 조회해 최신 기준일의 지수 정보를 upsert하고,
   * 항목별 처리 결과를 SyncJob 이력으로 저장해 반환한다.
   * 주말·휴일에는 당일 데이터가 없을 수 있어 최근 7일 중 최신 기준일을 사용한다.
   */
  public List<SyncJobDto> syncIndexInfo(String worker) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    List<Item> items = openApiClient.getIndexData(today.minusDays(7), today);
    List<Item> latestItems = filterLatestDate(items);

    return latestItems.stream()
        .map(item -> syncOneIndexInfo(item, LocalDate.parse(item.basDt(), KRX_DATE_FORMAT), worker))
        .toList();
  }

  /**
   * 요청한 기간의 지수 데이터를 연동한다.
   *
   * <p>indexInfoIds가 null이거나 비어 있으면 등록된 전체 지수가 대상이다.
   * Open API 응답에는 내부 DB id가 없으므로 지수 분류명과 지수명으로 대상을 연결한다.
   */
  public List<SyncJobDto> syncIndexData(
      IndexDataSyncRequest request,
      String worker
  ) {
    LocalDate from = request.baseDateFrom();
    LocalDate to = request.baseDateTo();

    if (from.isAfter(to)) {
      throw new IllegalArgumentException(
          "시작일은 종료일보다 늦을 수 없습니다"
      );
    }

    List<Long> indexInfoIds = request.indexInfoIds();

    if (indexInfoIds == null || indexInfoIds.isEmpty()) {
      indexInfoIds = indexInfoService.getSummaries().stream()
          .map(IndexInfoSummaryDto::id)
          .toList();
    }

    List<Item> items =
        openApiClient.getIndexData(from, to);

    return syncIndexDataItems(
        indexInfoIds,
        items,
        worker
    );
  }

  public void syncScheduledIndexData(List<Long> indexInfoIds, String worker) {
    if (indexInfoIds == null || indexInfoIds.isEmpty()) {
      return;
    }

    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    indexInfoIds = indexInfoIds.stream().distinct().toList();

    for (Long indexInfoId : indexInfoIds) {
      SyncJobSearchCondition condition = new SyncJobSearchCondition(
          JobType.INDEX_DATA,
          indexInfoId,
          null,
          null,
          worker,
          null,
          null,
          null,
          null,
          null,
          "targetDate",
          "desc",
          1
      );

      LocalDate startDate = syncJobRepository.findPageByCondition(condition)
          .stream()
          .findFirst()
          .map(SyncJob::getTargetDate)
          .orElse(today.minusDays(7));

      List<Item> items =
          openApiClient.getIndexData(startDate, today);

      syncIndexDataItems(List.of(indexInfoId), items, worker);
    }
  }

  /** 주말·휴일을 포함한 조회 범위에서 API가 제공한 가장 최근 영업일 항목만 반환한다. */
  private List<Item> filterLatestDate(List<Item> items) {
    if (items.isEmpty()) {
      throw new IllegalStateException("요청에는 성공했지만 지수 정보 데이터가 없습니다.");
    }

    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
    List<Item> result = new ArrayList<>();
    LocalDate latestApiDate = LocalDate.parse(items.get(0).basDt(), format);

    for (Item item : items) {
      LocalDate itemDate = LocalDate.parse(item.basDt(), format);
      if (itemDate.isAfter(latestApiDate)) {
        latestApiDate = itemDate;
      }
    }

    for (Item item : items) {
      LocalDate itemDate = LocalDate.parse(item.basDt(), format);
      if (itemDate.isEqual(latestApiDate)) {
        result.add(item);
      }
    }

    return result;
  }


  /**
   * Open API 항목 하나를 IndexInfo로 upsert하고 처리 결과를 SyncJob에 기록한다.
   * 처리 중 예외가 발생하면 IndexInfo가 없는 FAILED 이력을 기록한다.
   */
  private SyncJobDto syncOneIndexInfo(
      Item item,
      LocalDate targetDate,
      String worker
  ) {
    try{
      IndexInfoCreateRequest infoDto  = IndexInfoCreateRequest.fromOpenApi(item);
      IndexInfo savedIndexInfo = indexInfoService.upsertFromOpenApi(
          infoDto.indexClassification(),
          infoDto.indexName(),
          infoDto.employedItemsCount(),
          infoDto.basePointInTime(),
          infoDto.baseIndex()
      );

      SyncJob successJob = SyncJob.create(
          JobType.INDEX_INFO,
          savedIndexInfo,
          targetDate,
          worker,
          ResultType.SUCCESS
      );

      SyncJob savedJob = syncJobRepository.save(successJob);

      return SyncJobDto.toSyncJobDto(savedJob);

    } catch (Exception e) {
      log.warn(
          "지수 정보 연동 실패: classification={}, name={}, reason={}",
          item.idxCsf(), item.idxNm(), e.getMessage(), e);
      SyncJob failedJob = SyncJob.create(
          JobType.INDEX_INFO,
          null,
          targetDate,
          worker,
          ResultType.FAILED
      );

      SyncJob savedJob = syncJobRepository.save(failedJob);

      return SyncJobDto.toSyncJobDto(savedJob);
    }
  }


  /**
   * Open API 항목 하나를 IndexData로 upsert하고 SyncJob 이력을 기록한다.
   * 처리 실패는 FAILED 이력으로 변환해 다음 항목의 연동을 계속한다.
   */
  private SyncJobDto syncOneIndexData(
      Item item,
      IndexInfo indexInfo,
      String worker
  ) {
    LocalDate targetDate = LocalDate.parse(item.basDt(), KRX_DATE_FORMAT);

    try{
      IndexDataCreateRequest dataDto  = IndexDataCreateRequest.fromOpenApi(indexInfo.getId(), item);

      indexDataService.upsertFromOpenApi(dataDto);

      SyncJob successJob = SyncJob.create(
          JobType.INDEX_DATA,
          indexInfo,
          targetDate,
          worker,
          ResultType.SUCCESS
      );

      SyncJob savedJob = syncJobRepository.save(successJob);

      return SyncJobDto.toSyncJobDto(savedJob);

    } catch (Exception e) {
      log.warn(
          "지수 데이터 연동 실패: indexInfoId={}, baseDate={}, reason={}",
          indexInfo.getId(), item.basDt(), e.getMessage(), e);
      SyncJob failedJob = SyncJob.create(
          JobType.INDEX_DATA,
          indexInfo,
          targetDate,
          worker,
          ResultType.FAILED
      );

      SyncJob savedJob = syncJobRepository.save(failedJob);

      return SyncJobDto.toSyncJobDto(savedJob);
    }
  }

  private List<SyncJobDto> syncIndexDataItems(
      List<Long> indexInfoIds,
      List<Item> items,
      String worker
  ) {
    List<SyncJobDto> result = new ArrayList<>();

    List<IndexInfo> targetIndexInfos = indexInfoIds.stream()
        .distinct()
        .map(indexInfoService::getEntity)
        .toList();

    // Open API에는 내부 id가 없으므로 (지수 분류명, 지수명)을 연결 키로 사용한다.
    Map<IndexKey, IndexInfo> targetIndexMap =
        targetIndexInfos.stream()
            .collect(Collectors.toMap(
                indexInfo -> new IndexKey(
                    indexInfo.getIndexClassification(),
                    indexInfo.getIndexName()
                ),
                indexInfo -> indexInfo
            ));

    for (Item item: items) {
      IndexKey indexKey = new IndexKey(
          item.idxCsf(),
          item.idxNm()
      );

      IndexInfo targetIndexInfo = targetIndexMap.get(indexKey);

      // API는 기간 내 전체 지수를 반환하므로 요청 대상이 아닌 항목은 건너뛴다.
      if (targetIndexInfo == null) {
        continue;
      }

      result.add(syncOneIndexData(item, targetIndexInfo, worker));
    }

    return result;
  }

  /** 내부 id가 없는 Open API 항목을 IndexInfo와 연결하기 위한 키. */
  private record IndexKey (
      String indexClassification,
      String indexName
  ) {}
}
