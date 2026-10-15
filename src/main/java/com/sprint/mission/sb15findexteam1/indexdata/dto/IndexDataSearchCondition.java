package com.sprint.mission.sb15findexteam1.indexdata.dto;

import java.time.LocalDate;

public record IndexDataSearchCondition(
    Long indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {
    // TODO(A): 커서 해석/공통 페이지 응답 규칙과 연결
    // TODO(B): sortField 10개 허용값 검사 및 기본값(baseDate desc, size 10) 적용
}
