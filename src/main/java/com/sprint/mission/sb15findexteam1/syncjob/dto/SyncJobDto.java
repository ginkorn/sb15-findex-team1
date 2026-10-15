package com.sprint.mission.sb15findexteam1.syncjob.dto;

/**
 * 연동 작업 응답 DTO. C(실행 결과)와 D(이력 목록)가 같이 쓴다.
 *
 * <p>TODO: Swagger 타입에 맞추기 (id·indexInfoId → Long, 날짜 → LocalDate/LocalDateTime, jobType·result → enum)
 */
public record SyncJobDto(
    int id,
    String jobType,
    int indexInfoId,
    String targetDate,
    String worker,
    String jobTime,
    String result
) {

}
