package com.sprint.mission.sb15findexteam1.indexinfo.repository;

import static com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition.SORT_EMPLOYED_ITEMS_COUNT;
import static com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition.SORT_NAME;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.common.exception.BusinessException;
import com.sprint.mission.sb15findexteam1.common.exception.ErrorCode;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.QIndexInfo;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * 지수 정보 목록 검색 구현 (QueryDSL).
 *
 * <p><b>QueryDSL이란?</b> SQL을 문자열 대신 자바 코드로 쓰게 해 주는 도구다.
 * 오타가 있으면 실행 전에 컴파일 에러로 알려 주고, 조건을 레고처럼 붙였다 뗐다 할 수 있다.
 * {@code QIndexInfo}는 IndexInfo 엔티티를 보고 빌드할 때 자동으로 만들어지는 "쿼리용 클래스"다.
 *
 * <p><b>동적 쿼리 만드는 법</b>: 조건 메서드는 값이 없으면 null을 돌려준다.
 * QueryDSL의 where()는 null 조건을 무시하므로 "값이 있을 때만 조건이 붙는" 쿼리가 된다.
 * <pre>
 * indexName = "코스", favorite = null 이면
 * → where index_name like '%코스%'           (favorite 조건은 빠짐)
 * </pre>
 *
 * <p>이 클래스는 IndexInfoRepository가 IndexInfoQueryRepository를 상속하고 있어서,
 * 서비스에서 {@code indexInfoRepository.findPageByCondition(...)}처럼 바로 쓸 수 있다.
 */
public class IndexInfoQueryRepositoryImpl implements IndexInfoQueryRepository {

  /** 쿼리에서 "index_info 테이블"을 가리키는 객체 */
  private static final QIndexInfo indexInfo = QIndexInfo.indexInfo;

  /** QueryDSL 쿼리를 만들어 실행하는 객체 */
  private final JPAQueryFactory queryFactory;

  // EntityManager로 직접 만든다. (@DataJpaTest처럼 JPA 빈만 띄우는 테스트에서도 동작하도록,
  //  B·D의 QueryRepository와 같은 방식)
  public IndexInfoQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /**
   * 한 페이지 조회: 필터 + 커서 조건으로 거르고, 정렬한 뒤, size + 1개만 가져온다.
   * (+1개는 다음 페이지가 있는지 확인하는 용도. CursorPageResponseDto.of()가 잘라낸다)
   */
  @Override
  public List<IndexInfo> findPageByCondition(IndexInfoSearchCondition condition) {
    return queryFactory.selectFrom(indexInfo)
        .where(
            classificationContains(condition.indexClassification()),
            nameContains(condition.indexName()),
            favoriteEq(condition.favorite()),
            cursorCondition(condition)
        )
        .orderBy(orderBy(condition))
        .limit(condition.size() + 1L)
        .fetch();
  }

  /**
   * 전체 개수: 필터 조건만 적용한다. (커서는 빼야 "몇 번째 페이지든 같은 전체 개수"가 나온다)
   */
  @Override
  public long countByCondition(IndexInfoSearchCondition condition) {
    Long total = queryFactory.select(indexInfo.count())
        .from(indexInfo)
        .where(
            classificationContains(condition.indexClassification()),
            nameContains(condition.indexName()),
            favoriteEq(condition.favorite())
        )
        .fetchOne();
    return total == null ? 0 : total;
  }

  // ---- 필터 조건: 값이 없으면 null → where()에서 빠짐 ----

  /** 분류명 부분 일치 (SQL의 like '%값%') */
  private BooleanExpression classificationContains(String value) {
    return StringUtils.hasText(value) ? indexInfo.indexClassification.contains(value) : null;
  }

  /** 지수명 부분 일치 */
  private BooleanExpression nameContains(String value) {
    return StringUtils.hasText(value) ? indexInfo.indexName.contains(value) : null;
  }

  /** 즐겨찾기 정확히 일치 */
  private BooleanExpression favoriteEq(Boolean value) {
    return value != null ? indexInfo.favorite.eq(value) : null;
  }

  // ---- 커서와 정렬 ----

  /**
   * "이전 페이지 마지막 항목 다음부터" 조건.
   * cursor는 문자열로 오기 때문에 정렬 필드의 타입에 맞게 바꿔서 비교한다.
   * (분류명·지수명은 문자열 그대로, 채용 종목 수는 숫자로 변환)
   */
  private BooleanExpression cursorCondition(IndexInfoSearchCondition condition) {
    String cursor = condition.cursor();
    Long idAfter = condition.idAfter();
    if (cursor == null || idAfter == null) {
      return null; // 첫 페이지: 조건 없이 처음부터
    }
    boolean asc = condition.isAsc();
    return switch (condition.sortField()) {
      case SORT_NAME -> CursorPaging.after(indexInfo.indexName, cursor, indexInfo.id, idAfter, asc);
      case SORT_EMPLOYED_ITEMS_COUNT -> CursorPaging.after(
          indexInfo.employedItemsCount, parseInteger(cursor), indexInfo.id, idAfter, asc);
      default -> CursorPaging.after(indexInfo.indexClassification, cursor, indexInfo.id, idAfter, asc);
    };
  }

  /** 정렬: "선택한 정렬 필드 → id" 순서 (cursorCondition과 반드시 같은 필드·방향) */
  private OrderSpecifier<?>[] orderBy(IndexInfoSearchCondition condition) {
    boolean asc = condition.isAsc();
    return switch (condition.sortField()) {
      case SORT_NAME -> CursorPaging.orderBy(indexInfo.indexName, indexInfo.id, asc);
      case SORT_EMPLOYED_ITEMS_COUNT -> CursorPaging.orderBy(indexInfo.employedItemsCount, indexInfo.id, asc);
      default -> CursorPaging.orderBy(indexInfo.indexClassification, indexInfo.id, asc);
    };
  }

  /** 숫자 cursor 변환. 숫자가 아닌 값이 오면 500이 아니라 400으로 응답하도록 BusinessException을 던진다. */
  private Integer parseInteger(String cursor) {
    try {
      return Integer.valueOf(cursor);
    } catch (NumberFormatException e) {
      throw new BusinessException(ErrorCode.INVALID_INPUT, "cursor=" + cursor);
    }
  }
}
