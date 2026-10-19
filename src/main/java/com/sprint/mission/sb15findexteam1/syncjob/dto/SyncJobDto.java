package com.sprint.mission.sb15findexteam1.syncjob.dto;

import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 연동 작업 응답 DTO. C(실행 결과)와 D(이력 목록)가 같이 쓴다.
 */
public record SyncJobDto(
    Long id,
    JobType jobType,
    Long indexInfoId,
    LocalDate targetDate,
    String worker,
    LocalDateTime jobTime,
    ResultType result
) {
  /**
   * 엔티티를 응답 DTO로 변환한다.
   * 지수 정보 연동 실패 또는 지수 삭제 이력은 indexInfoId가 null일 수 있다.
   */
  public static SyncJobDto toSyncJobDto(SyncJob syncJob) {
    Long indexInfoId = syncJob.getIndexInfo() == null
        ? null
        : syncJob.getIndexInfo().getId();

    return new SyncJobDto(
        syncJob.getId(),
        syncJob.getJobType(),
        indexInfoId,
        syncJob.getTargetDate(),
        syncJob.getWorker(),
        syncJob.getJobTime(),
        syncJob.getResult()
    );
  }
}
