package com.sprint.mission.sb15findexteam1.autosyncconfig.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.util.Set;

/**
 * 자동 연동 설정 목록 조회 조건 = GET /api/auto-sync-configs 의 쿼리 파라미터 묶음.
 *
 * <table>
 *   <tr><th>파라미터</th><th>규칙</th></tr>
 *   <tr><td>indexInfoId</td><td>특정 지수의 설정만</td></tr>
 *   <tr><td>enabled</td><td>true = 켜진 것만, false = 꺼진 것만, 없으면 전부</td></tr>
 *   <tr><td>idAfter, cursor</td><td>다음 페이지 요청용 (이전 응답의 nextIdAfter, nextCursor)</td></tr>
 *   <tr><td>sortField</td><td>indexInfo.indexName(기본, 지수명) / enabled</td></tr>
 *   <tr><td>sortDirection</td><td>asc(기본) / desc</td></tr>
 *   <tr><td>size</td><td>기본 10, 최대 100</td></tr>
 * </table>
 *
 * <p>기본값을 채우는 방식은 IndexInfoSearchCondition과 같다. (아래 compact constructor)
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

  // 정렬 필드 이름 (Swagger의 기본 정렬값이 "indexInfo.indexName"이라 이 글자 그대로 쓴다)
  public static final String SORT_INDEX_NAME = "indexInfo.indexName";
  public static final String SORT_ENABLED = "enabled";

  private static final Set<String> SORT_FIELDS = Set.of(SORT_INDEX_NAME, SORT_ENABLED);

  /** 값이 없거나 허용되지 않은 값이면 기본값으로 맞춘다. */
  public AutoSyncConfigSearchCondition {
    if (sortField == null || !SORT_FIELDS.contains(sortField)) {
      sortField = SORT_INDEX_NAME;
    }
    sortDirection = CursorPaging.isAsc(sortDirection) ? "asc" : "desc";
    size = CursorPaging.normalizeSize(size);
  }

  /** 오름차순이면 true */
  public boolean isAsc() {
    return "asc".equals(sortDirection);
  }

  /** 활성화 여부로 정렬하면 true, 지수명으로 정렬하면 false */
  public boolean sortByEnabled() {
    return SORT_ENABLED.equals(sortField);
  }
}
