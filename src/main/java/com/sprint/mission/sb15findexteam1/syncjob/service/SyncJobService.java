package com.sprint.mission.sb15findexteam1.syncjob.service;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataService;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.service.IndexInfoService;
import com.sprint.mission.sb15findexteam1.openapi.client.OpenApiClient;
import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto.Item;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import com.sprint.mission.sb15findexteam1.syncjob.repository.SyncJobRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

  /**
   * todo: IndexData의 upsert가 완성된 뒤에 진행
  public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request) {
    LocalDate from = LocalDate.parse(request.baseDateFrom());
    LocalDate to = LocalDate.parse(request.baseDateTo());

    if (from.isAfter(to)) throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다");

    List<Item> items = openApiClient.getIndexData(from, to);

    return latestItems.stream()
        .map(item -> syncOneIndexData(item, LocalDate.now(), r))
        .toList();
  }
**/

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
   * TODO: IndexData upsert 구현 후 Open API 항목을 저장하고 처리 결과를 기록한다.
   * 현재는 실제 IndexData 저장과 대상 IndexInfo 연결이 구현되지 않았다.
   */
  private SyncJobDto syncOneIndexData(
      Item item,
      LocalDate targetDate,
      String worker
  ) {
    try{
      IndexDataCreateRequest dataDto  = IndexDataCreateRequest.fromOpenApi(item);
      /*
      todo: upsertFromOpenApi가 완성되면 진행.
      IndexData savedIndexData = indexDataService.upsertFromOpenApi(
          dataDto.
      ); */

      SyncJob successJob = SyncJob.create(
          JobType.INDEX_DATA,
          null, // todo: upsertFromOpenApi가 완성되면 진행.
          targetDate,
          worker,
          ResultType.SUCCESS
      );

      SyncJob savedJob = syncJobRepository.save(successJob);

      return SyncJobDto.toSyncJobDto(savedJob);

    } catch (Exception e) {
      SyncJob failedJob = SyncJob.create(
          JobType.INDEX_DATA,
          null,
          targetDate,
          worker,
          ResultType.FAILED
      );

      SyncJob savedJob = syncJobRepository.save(failedJob);

      return SyncJobDto.toSyncJobDto(savedJob);
    }
  }
}
