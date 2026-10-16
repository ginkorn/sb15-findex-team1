package com.sprint.mission.sb15findexteam1.syncjob.dto;

import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import java.time.LocalDate;

/**
 * 연동 작업 응답 DTO. C(실행 결과)와 D(이력 목록)가 같이 쓴다.
 */
public record SyncJobDto(
    Long id,
    JobType jobType,
    IndexInfo indexInfo,
    LocalDate targetDate,
    String worker,
    String jobTime,
    ResultType result
) {

}
