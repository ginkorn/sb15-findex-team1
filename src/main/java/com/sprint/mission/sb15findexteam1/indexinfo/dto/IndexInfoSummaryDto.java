package com.sprint.mission.sb15findexteam1.indexinfo.dto;

/**
 * 지수 요약 정보 (GET /api/index-infos/summaries).
 * 프론트엔드의 "지수 선택" 드롭다운에서 쓴다.
 */
public record IndexInfoSummaryDto(
    Long id,
    String indexClassification,
    String indexName
) {
}
