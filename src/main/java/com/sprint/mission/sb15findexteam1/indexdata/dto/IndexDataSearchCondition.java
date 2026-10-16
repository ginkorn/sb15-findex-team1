
package com.sprint.mission.sb15findexteam1.indexdata.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.time.LocalDate;
import java.util.Set;

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

  // 정렬 가능한 필드 10개
  private static final Set<String> SORT_FIELDS = Set.of(
      "baseDate",
      "marketPrice",
      "closingPrice",
      "highPrice",
      "lowPrice",
      "versus",
      "fluctuationRate",
      "tradingQuantity",
      "tradingPrice",
      "marketTotalAmount"
  );

  // record 객체가 생성될 때 검색 조건을 정리
  public IndexDataSearchCondition {

    // 정렬 필드가 없거나 허용되지 않으면 baseDate 사용
    if (sortField == null || !SORT_FIELDS.contains(sortField)) {
      sortField = "baseDate";
    }

    // B 파트의 기본 정렬 방향은 desc로 설정
    if (sortDirection == null || sortDirection.isBlank()) {
      sortDirection = "desc";
    } else {
      // A 파트 공통 규칙을 사용해 정렬 방향 정규화
      sortDirection = CursorPaging.isAsc(sortDirection)
          ? "asc"
          : "desc";
    }

    // 기본 10개, 최대 100개
    size = CursorPaging.normalizeSize(size);
  }
}