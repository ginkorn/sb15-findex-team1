package com.sprint.mission.sb15findexteam1.syncjob.dto;

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
