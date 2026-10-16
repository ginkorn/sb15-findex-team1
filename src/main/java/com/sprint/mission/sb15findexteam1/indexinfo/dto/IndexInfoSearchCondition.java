package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.util.Set;

/**
 * 지수 정보 목록 조회 조건 = GET /api/index-infos 의 쿼리 파라미터 묶음.
 *
 * <p>예: {@code /api/index-infos?indexName=코스&favorite=true&sortField=indexName&sortDirection=desc&size=20}
 * 파라미터 이름과 아래 필드 이름이 같으면 컨트롤러의 {@code @ModelAttribute}가 자동으로 채워 준다.
 *
 * <table>
 *   <tr><th>파라미터</th><th>규칙</th></tr>
 *   <tr><td>indexClassification, indexName</td><td>일부만 맞아도 검색 ("코스" → 코스피, 코스닥)</td></tr>
 *   <tr><td>favorite</td><td>true/false 정확히 일치. 보내지 않으면 필터하지 않음</td></tr>
 *   <tr><td>idAfter, cursor</td><td>다음 페이지 요청용. 이전 응답의 nextIdAfter, nextCursor를 그대로 보냄</td></tr>
 *   <tr><td>sortField</td><td>indexClassification(기본) / indexName / employedItemsCount</td></tr>
 *   <tr><td>sortDirection</td><td>asc(기본) / desc</td></tr>
 *   <tr><td>size</td><td>기본 10, 최대 100</td></tr>
 * </table>
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

  // 정렬 필드 이름 (Swagger 명세의 sortField 값과 같아야 한다)
  public static final String SORT_CLASSIFICATION = "indexClassification";
  public static final String SORT_NAME = "indexName";
  public static final String SORT_EMPLOYED_ITEMS_COUNT = "employedItemsCount";

  /** 허용하는 정렬 필드 목록. 이 외의 값이 오면 기본값으로 바꾼다. */
  private static final Set<String> SORT_FIELDS =
      Set.of(SORT_CLASSIFICATION, SORT_NAME, SORT_EMPLOYED_ITEMS_COUNT);

  /**
   * 기본값을 채우는 곳 (record의 "compact constructor").
   *
   * <p>record가 만들어질 때 자동으로 실행되며, 여기서 바꾼 값이 그대로 필드에 들어간다.
   * 그래서 서비스·Repository에서는 null 걱정 없이 sortField, sortDirection, size를 쓸 수 있다.
   * <ul>
   *   <li>sortField가 없거나 허용되지 않은 값 → "indexClassification"</li>
   *   <li>sortDirection → "asc" 또는 "desc" 둘 중 하나로 정리</li>
   *   <li>size → 기본 10, 최대 100 (CursorPaging.normalizeSize)</li>
   * </ul>
   */
  public IndexInfoSearchCondition {
    if (sortField == null || !SORT_FIELDS.contains(sortField)) {
      sortField = SORT_CLASSIFICATION;
    }
    sortDirection = CursorPaging.isAsc(sortDirection) ? "asc" : "desc";
    size = CursorPaging.normalizeSize(size);
  }

  /** 오름차순이면 true */
  public boolean isAsc() {
    return "asc".equals(sortDirection);
  }
}
