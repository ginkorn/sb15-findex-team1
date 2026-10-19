package com.sprint.mission.sb15findexteam1.syncjob.dto;

import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record SyncJobSearchCondition(
    JobType jobType,
    Long indexInfoId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateTo,
    String worker,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobTimeFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobTimeTo,
    ResultType status,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

  // 조건이 하나도 없는 검색 조건 (테스트와 기본 조회용)
  public static SyncJobSearchCondition empty() {
    return new SyncJobSearchCondition(null, null, null, null,
        null, null, null, null, null, null, null, null, null);
  }

  // 페이지 크기만 바꾼 복사본
  public SyncJobSearchCondition withSize(Integer size) {
    return new SyncJobSearchCondition(jobType, indexInfoId, baseDateFrom, baseDateTo, worker,
        jobTimeFrom, jobTimeTo, status, idAfter, cursor, sortField, sortDirection, size);
  }
}