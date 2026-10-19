package com.sprint.mission.sb15findexteam1.syncjob.dto;

import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobSearchCondition(
    JobType jobType,                // 작업 유형
    Long indexInfoId,               // 지수
    LocalDate baseDateFrom,         // 대상 날짜 시작
    LocalDate baseDateTo,           // 대상 날짜 끝
    String worker,                  // 작업자
    LocalDateTime jobTimeFrom,      // 작업 일시 시작
    LocalDateTime jobTimeTo,        // 작업 일시 끝
    ResultType status,              // 결과
    Long idAfter,                   // 커서: 이전 페이지 마지막 id
    String cursor,                  // 커서: 이전 페이지 마지막 정렬 값
    String sortField,               // 정렬 기준 (targetDate, jobTime)
    String sortDirection,           // 정렬 방향 (asc, desc)
    Integer size                    // 페이지 크기
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