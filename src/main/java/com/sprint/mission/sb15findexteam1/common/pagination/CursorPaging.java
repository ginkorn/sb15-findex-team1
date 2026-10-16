package com.sprint.mission.sb15findexteam1.common.pagination;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;

/**
 * 커서 페이지네이션의 팀 공통 규칙과 QueryDSL 도우미 모음.
 *
 * <h3>커서 방식이란? (책갈피)</h3>
 * <ul>
 *   <li>흔한 방식(offset): "11번째부터 10개 주세요". 그 사이 데이터가 추가·삭제되면
 *       순서가 밀려서 같은 항목이 두 번 나오거나 빠진다.</li>
 *   <li>커서 방식: "마지막으로 본 게 이거였어요, 그 다음 10개 주세요". 책갈피처럼 위치를 기억해서 밀리지 않는다.</li>
 * </ul>
 *
 * <h3>프론트엔드가 보내는 값</h3>
 * <ul>
 *   <li>cursor: 이전 페이지 마지막 항목의 <b>정렬 필드 값</b> (예: 지수명 정렬이면 "코스피 200")</li>
 *   <li>idAfter: 이전 페이지 마지막 항목의 <b>id</b></li>
 * </ul>
 *
 * <h3>왜 cursor와 idAfter가 둘 다 필요한가?</h3>
 * 분류명으로 정렬하면 "KOSPI시리즈"가 30개일 수 있다. 값만으로는 30개 중 어디까지 봤는지 모르므로,
 * 값이 같으면 id로 한 번 더 구분한다. 그래서 정렬도 항상 "정렬 필드 → id" 순서로 한다.
 *
 * <pre>
 * 다음 페이지 조건 (오름차순 기준, 내림차순이면 &gt; 가 &lt; 로 바뀜)
 * (정렬값 &gt; cursor)  OR  (정렬값 = cursor AND id &gt; idAfter)
 * </pre>
 *
 * <h3>팀 공통 정책</h3>
 * <ul>
 *   <li>size: 기본 10, 최대 100 → {@link #normalizeSize(Integer)}</li>
 *   <li>정렬 값이 null인 행은 cursor를 문자열 "__NULL__"로 주고받는다 → {@link #NULL_CURSOR}</li>
 *   <li>sortDirection: "desc"만 내림차순, 나머지(값 없음 포함)는 오름차순 → {@link #isAsc(String)}</li>
 * </ul>
 *
 * <p>사용 예시는 IndexInfoQueryRepositoryImpl, AutoSyncConfigQueryRepositoryImpl을 참고.
 */
public final class CursorPaging {

  /** size를 보내지 않았을 때의 페이지 크기 */
  public static final int DEFAULT_SIZE = 10;

  /** 한 번에 줄 수 있는 최대 개수 (너무 크게 요청해 서버가 느려지는 것을 막는다) */
  public static final int MAX_SIZE = 100;

  /** 정렬 값이 null인 행을 cursor로 표현할 때 쓰는 약속된 문자열 (B와 협의한 값) */
  public static final String NULL_CURSOR = "__NULL__";

  /** 도구 모음 클래스라 객체를 만들 필요가 없다. (new CursorPaging() 금지) */
  private CursorPaging() {
  }

  /**
   * size를 팀 정책에 맞게 고친다.
   * 없거나 0 이하 → 10, 100 초과 → 100, 그 외 → 그대로.
   */
  public static int normalizeSize(Integer size) {
    if (size == null || size <= 0) {
      return DEFAULT_SIZE;
    }
    return Math.min(size, MAX_SIZE);
  }

  /** "desc"(대소문자 무관)면 false(내림차순), 그 외에는 true(오름차순) */
  public static boolean isAsc(String sortDirection) {
    return !"desc".equalsIgnoreCase(sortDirection);
  }

  /** 정렬 값을 응답의 nextCursor 문자열로 바꾼다. null이면 "__NULL__" */
  public static String toCursor(Object sortValue) {
    return sortValue == null ? NULL_CURSOR : sortValue.toString();
  }

  /**
   * 다음 페이지 조건을 만든다. (문자열·날짜·boolean으로 정렬할 때)
   *
   * <p>cursor나 idAfter가 없으면 첫 페이지라는 뜻이라 null을 돌려준다.
   * QueryDSL의 where()는 null 조건을 무시하므로, 첫 페이지에서는 조건 없이 처음부터 조회된다.
   *
   * <pre>
   * .where(..., CursorPaging.after(indexInfo.indexName, cursor, indexInfo.id, idAfter, asc))
   * </pre>
   *
   * @param sortPath 정렬 기준 컬럼 (예: indexInfo.indexName)
   * @param cursor   이전 페이지 마지막 항목의 정렬 값 (정렬 컬럼과 같은 타입으로 변환해서 넘긴다)
   * @param idPath   id 컬럼 (예: indexInfo.id)
   * @param idAfter  이전 페이지 마지막 항목의 id
   * @param asc      오름차순이면 true
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T extends Comparable> BooleanExpression after(
      ComparableExpression<T> sortPath, T cursor,
      NumberPath<Long> idPath, Long idAfter, boolean asc) {
    if (cursor == null || idAfter == null) {
      return null;
    }
    // 정렬값이 cursor보다 뒤에 있는 행
    BooleanExpression beyond = asc ? sortPath.gt(cursor) : sortPath.lt(cursor);
    // 정렬값이 cursor와 같으면 id로 구분해서 뒤에 있는 행
    BooleanExpression tie = sortPath.eq(cursor).and(asc ? idPath.gt(idAfter) : idPath.lt(idAfter));
    return beyond.or(tie);
  }

  /**
   * 다음 페이지 조건을 만든다. (숫자로 정렬할 때: 채용 종목 수, 종가, 거래량 등)
   * 사용법은 위 메서드와 같다. cursor 문자열은 Integer.valueOf, new BigDecimal 등으로 바꿔서 넘긴다.
   */
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

  /**
   * "정렬 필드 → id" 순서의 정렬 조건을 만든다. after()와 반드시 같은 방향으로 써야 한다.
   *
   * <pre>
   * .orderBy(CursorPaging.orderBy(indexInfo.indexName, indexInfo.id, asc))
   * </pre>
   */
  public static OrderSpecifier<?>[] orderBy(ComparableExpressionBase<?> sortPath,
      NumberPath<Long> idPath, boolean asc) {
    return new OrderSpecifier<?>[]{
        asc ? sortPath.asc() : sortPath.desc(),
        asc ? idPath.asc() : idPath.desc()
    };
  }
}
