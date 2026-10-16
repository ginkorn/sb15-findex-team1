package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.util.Set;

/**
 * 지수 정보 목록 조회 조건 (GET /api/index-infos 의 쿼리 파라미터).
 *
 * <ul>
 *   <li>indexClassification, indexName: 일부만 맞아도 검색 ("코스" → 코스피, 코스닥)</li>
 *   <li>favorite: 정확히 일치. 보내지 않으면 필터하지 않음</li>
 *   <li>sortField: indexClassification(기본) / indexName / employedItemsCount</li>
 *   <li>sortDirection: asc(기본) / desc</li>
 *   <li>size: 기본 10, 최대 100</li>
 * </ul>
 * 값이 없거나 허용되지 않은 값이면 기본값으로 맞춘다.
 */
public record IndexInfoSearchCondition(
    String indexClassification,
    String indexName,
    Boolean favorite,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

  public static final String SORT_CLASSIFICATION = "indexClassification";
  public static final String SORT_NAME = "indexName";
  public static final String SORT_EMPLOYED_ITEMS_COUNT = "employedItemsCount";

  private static final Set<String> SORT_FIELDS =
      Set.of(SORT_CLASSIFICATION, SORT_NAME, SORT_EMPLOYED_ITEMS_COUNT);

  public IndexInfoSearchCondition {
    if (sortField == null || !SORT_FIELDS.contains(sortField)) {
      sortField = SORT_CLASSIFICATION;
    }
    sortDirection = CursorPaging.isAsc(sortDirection) ? "asc" : "desc";
    size = CursorPaging.normalizeSize(size);
  }

  public boolean isAsc() {
    return "asc".equals(sortDirection);
  }
}
