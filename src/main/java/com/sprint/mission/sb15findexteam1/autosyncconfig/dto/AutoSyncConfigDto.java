package com.sprint.mission.sb15findexteam1.autosyncconfig.dto;

public record AutoSyncConfigDto(
    int id,
    int indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled) {

}
