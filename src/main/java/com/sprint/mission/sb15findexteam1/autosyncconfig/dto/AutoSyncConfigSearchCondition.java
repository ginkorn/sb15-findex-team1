package com.sprint.mission.sb15findexteam1.autosyncconfig.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.util.Set;

/**
 * 자동 연동 설정 목록 조회 조건 (GET /api/auto-sync-configs 의 쿼리 파라미터).
 *
 * <ul>
 *   <li>indexInfoId: 특정 지수만, enabled: 켜진 것만 / 꺼진 것만</li>
 *   <li>sortField: indexInfo.indexName(기본, 지수명) / enabled</li>
 *   <li>sortDirection: asc(기본) / desc, size: 기본 10, 최대 100</li>
 * </ul>
 */
public record AutoSyncConfigSearchCondition(
    Long indexInfoId,
    Boolean enabled,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

  public static final String SORT_INDEX_NAME = "indexInfo.indexName";
  public static final String SORT_ENABLED = "enabled";

  private static final Set<String> SORT_FIELDS = Set.of(SORT_INDEX_NAME, SORT_ENABLED);

  public AutoSyncConfigSearchCondition {
    if (sortField == null || !SORT_FIELDS.contains(sortField)) {
      sortField = SORT_INDEX_NAME;
    }
    sortDirection = CursorPaging.isAsc(sortDirection) ? "asc" : "desc";
    size = CursorPaging.normalizeSize(size);
  }

  public boolean isAsc() {
    return "asc".equals(sortDirection);
  }

  public boolean sortByEnabled() {
    return SORT_ENABLED.equals(sortField);
  }
}
