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
 * <p>조건 메서드는 값이 없으면 null을 돌려준다. QueryDSL의 where()는 null 조건을 무시하므로
 * "값이 있을 때만 조건을 붙이는" 동적 쿼리가 된다.
 */
public class IndexInfoQueryRepositoryImpl implements IndexInfoQueryRepository {

  private static final QIndexInfo indexInfo = QIndexInfo.indexInfo;

  private final JPAQueryFactory queryFactory;

  // EntityManager로 직접 만든다. (@DataJpaTest처럼 JPA 빈만 띄우는 테스트에서도 동작하도록,
  //  B·D의 QueryRepository와 같은 방식)
  public IndexInfoQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

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

  // ---- 필터: 값이 없으면 null → 조건에서 빠짐 ----

  private BooleanExpression classificationContains(String value) {
    return StringUtils.hasText(value) ? indexInfo.indexClassification.contains(value) : null;
  }

  private BooleanExpression nameContains(String value) {
    return StringUtils.hasText(value) ? indexInfo.indexName.contains(value) : null;
  }

  private BooleanExpression favoriteEq(Boolean value) {
    return value != null ? indexInfo.favorite.eq(value) : null;
  }

  // ---- 커서: 정렬 필드에 맞게 cursor 문자열을 원래 타입으로 바꿔 "다음 페이지" 조건을 만든다 ----

  private BooleanExpression cursorCondition(IndexInfoSearchCondition condition) {
    String cursor = condition.cursor();
    Long idAfter = condition.idAfter();
    if (cursor == null || idAfter == null) {
      return null; // 첫 페이지
    }
    boolean asc = condition.isAsc();
    return switch (condition.sortField()) {
      case SORT_NAME -> CursorPaging.after(indexInfo.indexName, cursor, indexInfo.id, idAfter, asc);
      case SORT_EMPLOYED_ITEMS_COUNT -> CursorPaging.after(
          indexInfo.employedItemsCount, parseInteger(cursor), indexInfo.id, idAfter, asc);
      default -> CursorPaging.after(indexInfo.indexClassification, cursor, indexInfo.id, idAfter, asc);
    };
  }

  private OrderSpecifier<?>[] orderBy(IndexInfoSearchCondition condition) {
    boolean asc = condition.isAsc();
    return switch (condition.sortField()) {
      case SORT_NAME -> CursorPaging.orderBy(indexInfo.indexName, indexInfo.id, asc);
      case SORT_EMPLOYED_ITEMS_COUNT -> CursorPaging.orderBy(indexInfo.employedItemsCount, indexInfo.id, asc);
      default -> CursorPaging.orderBy(indexInfo.indexClassification, indexInfo.id, asc);
    };
  }

  private Integer parseInteger(String cursor) {
    try {
      return Integer.valueOf(cursor);
    } catch (NumberFormatException e) {
      throw new BusinessException(ErrorCode.INVALID_INPUT, "cursor=" + cursor);
    }
  }
}
