package com.sprint.mission.sb15findexteam1.autosyncconfig.dto;

/**
 * 자동 연동 설정 응답. 필드 이름은 Swagger의 AutoSyncConfigDto와 같다.
 * 화면에 지수 이름이 보여야 해서 지수 분류명·지수명을 함께 담는다.
 */
public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    Boolean enabled
) {
}
