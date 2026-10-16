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
  public static SyncJobDto toSyncJobDto(SyncJob syncJob) {
    /** 연결된 지수가 없으면 indexInfoId를 null로 변환한다. */
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
