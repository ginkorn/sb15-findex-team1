package com.sprint.mission.sb15findexteam1.syncjob.service;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataService;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.service.IndexInfoService;
import com.sprint.mission.sb15findexteam1.openapi.client.OpenApiClient;
import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto.Item;
import com.sprint.mission.sb15findexteam1.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import com.sprint.mission.sb15findexteam1.syncjob.repository.SyncJobRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobService {
  private static final DateTimeFormatter KRX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OpenApiClient openApiClient;
  private final SyncJobRepository syncJobRepository;
  private final IndexInfoService indexInfoService;
  private final IndexDataService indexDataService;

  /**
   * 최근 7일 데이터를 조회해 최신 기준일의 지수 정보를 upsert하고,
   * 항목별 처리 결과를 SyncJob 이력으로 저장해 반환한다.
   */
  public List<SyncJobDto> syncIndexInfo(String worker) {
    LocalDate today = LocalDate.now();
    List<Item> items = openApiClient.getIndexData(today.minusDays(7), today);
    List<Item> latestItems = filterLatestDate(items);

    return latestItems.stream()
        .map(item -> syncOneIndexInfo(item, LocalDate.parse(item.basDt(), KRX_DATE_FORMAT), worker))
        .toList();
  }

  public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String worker) {
    LocalDate from = request.baseDateFrom();
    LocalDate to = request.baseDateTo();

    if (from.isAfter(to)) throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다");

    List<Long> indexInfoIds = request.indexInfoIds();
    List<SyncJobDto> result = new ArrayList<>();


    // indexInfoIds가 비어있을 수도 있는데, 이러면 전체 지수를 조회한다는 뜻이므로
    // Summary 함수를 통해 전체 IndexInfo Id값을 불러와서 IndexInfoIds 변수를 채운다.
    // 그 뒤에 targetIndexInfos 에서 idxClassification과 idxNm으로 구체화 한다.
    if (indexInfoIds == null || indexInfoIds.isEmpty()) {
      indexInfoIds = indexInfoService.getSummaries().stream()
          .map(IndexInfoSummaryDto::id)
          .toList();
    }

    List<IndexInfo> targetIndexInfos = indexInfoIds.stream()
        .distinct()
        .map(indexInfoService::getEntity)
        .toList();

    Map<IndexKey, IndexInfo> targetIndexMap =
        targetIndexInfos.stream()
            .collect(Collectors.toMap(
                indexInfo -> new IndexKey(
                    indexInfo.getIndexClassification(),
                    indexInfo.getIndexName()
                ),
                indexInfo -> indexInfo
            ));

    List<Item> items = openApiClient.getIndexData(from, to);

    for (Item item: items) {
      IndexKey indexKey = new IndexKey(
          item.idxCsf(),
          item.idxNm()
      );

      IndexInfo targetIndexInfo = targetIndexMap.get(indexKey);

      if (targetIndexInfo == null) {
        continue;
      }

      result.add(syncOneIndexData(item, targetIndexInfo, worker));
    }

    return result;
  }

  /** API 응답 중 가장 최근 기준일의 항목만 반환한다. */
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
   *
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

  private record IndexKey (
      String indexClassification,
      String indexName
  ) {}
}
