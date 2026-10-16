package com.sprint.mission.sb15findexteam1.common.pagination;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;

/**
 * 커서 페이지네이션 공통 규칙과 QueryDSL 도우미.
 *
 * <p>커서 방식 = "책갈피" 방식. "11번째부터 주세요"(offset)가 아니라
 * "마지막으로 본 게 이거였어요, 그 다음을 주세요"라고 요청한다.
 *
 * <ul>
 *   <li>cursor: 이전 페이지 마지막 항목의 <b>정렬 필드 값</b></li>
 *   <li>idAfter: 이전 페이지 마지막 항목의 <b>id</b></li>
 * </ul>
 * 정렬 값이 같은 행이 여러 개일 수 있으므로 정렬은 항상 "정렬 필드 → id" 순서로 하고,
 * 다음 페이지 조건은 아래처럼 만든다. (오름차순 기준, 내림차순이면 부호 반대)
 * <pre>
 * (정렬값 &gt; cursor) OR (정렬값 = cursor AND id &gt; idAfter)
 * </pre>
 *
 * <p>팀 공통 정책
 * <ul>
 *   <li>size 기본 10, 최대 100 → {@link #normalizeSize(Integer)}</li>
 *   <li>정렬 값이 null이면 cursor 문자열 "__NULL__"로 주고받는다 → {@link #NULL_CURSOR}</li>
 * </ul>
 */
public final class CursorPaging {

  public static final int DEFAULT_SIZE = 10;
  public static final int MAX_SIZE = 100;

  /** 정렬 값이 null인 행을 cursor로 표현할 때 쓰는 약속된 문자열 (B와 협의한 값) */
  public static final String NULL_CURSOR = "__NULL__";

  private CursorPaging() {
  }

  /** size가 없거나 0 이하면 10, 100을 넘으면 100으로 맞춘다. */
  public static int normalizeSize(Integer size) {
    if (size == null || size <= 0) {
      return DEFAULT_SIZE;
    }
    return Math.min(size, MAX_SIZE);
  }

  /** "desc"면 내림차순, 그 외(없음 포함)는 오름차순 */
  public static boolean isAsc(String sortDirection) {
    return !"desc".equalsIgnoreCase(sortDirection);
  }

  /** 정렬 값을 응답의 nextCursor 문자열로 바꾼다. null이면 "__NULL__" */
  public static String toCursor(Object sortValue) {
    return sortValue == null ? NULL_CURSOR : sortValue.toString();
  }

  /**
   * 다음 페이지 조건 (문자열·날짜·boolean 정렬용).
   * cursor나 idAfter가 없으면(첫 페이지) null을 돌려준다. QueryDSL where()는 null 조건을 무시한다.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T extends Comparable> BooleanExpression after(
      ComparableExpression<T> sortPath, T cursor,
      NumberPath<Long> idPath, Long idAfter, boolean asc) {
    if (cursor == null || idAfter == null) {
      return null;
    }
    BooleanExpression beyond = asc ? sortPath.gt(cursor) : sortPath.lt(cursor);
    BooleanExpression tie = sortPath.eq(cursor).and(asc ? idPath.gt(idAfter) : idPath.lt(idAfter));
    return beyond.or(tie);
  }

  /** 다음 페이지 조건 (숫자 정렬용: 채용 종목 수, 종가 등) */
  public static <T extends Number & Comparable<?>> BooleanExpression after(
      NumberExpression<T> sortPath, T cursor,
      NumberPath<Long> idPath, Long idAfter, boolean asc) {
    if (cursor == null || idAfter == null) {
      return null;
    }
    BooleanExpression beyond = asc ? sortPath.gt(cursor) : sortPath.lt(cursor);
    BooleanExpression tie = sortPath.eq(cursor).and(asc ? idPath.gt(idAfter) : idPath.lt(idAfter));
    return beyond.or(tie);
  }

  /** "정렬 필드 → id" 순서의 정렬 조건 */
  public static OrderSpecifier<?>[] orderBy(ComparableExpressionBase<?> sortPath,
      NumberPath<Long> idPath, boolean asc) {
    return new OrderSpecifier<?>[]{
        asc ? sortPath.asc() : sortPath.desc(),
        asc ? idPath.asc() : idPath.desc()
    };
  }
}
