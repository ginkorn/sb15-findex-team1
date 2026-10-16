
package com.sprint.mission.sb15findexteam1.indexdata.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.entity.QIndexData;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class IndexDataQueryRepositoryImpl implements IndexDataQueryRepository {

  private static final QIndexData data = QIndexData.indexData;

  private final JPAQueryFactory queryFactory;

  public IndexDataQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  // 검색 조건 + 정렬 + 커서를 적용하여 한 페이지 조회
  @Override
  public List<IndexData> findPageByCondition(IndexDataSearchCondition condition) {
    String sortField = sortField(condition);
    boolean ascending = isAscending(condition);
    int size = CursorPaging.normalizeSize(condition.size());

    // 지수 ID와 날짜 조건 생성
    BooleanBuilder builder = createFilter(condition);

    // 이전 페이지의 마지막 데이터 이후부터 조회
    BooleanExpression cursorCondition =
        createCursorCondition(condition, sortField, ascending);

    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }

    // 다음 페이지 여부 판단을 위해 size보다 1개 더 조회
    return queryFactory
        .selectFrom(data)
        .where(builder)
        .orderBy(
            createOrder(sortField, ascending),
            ascending ? data.id.asc() : data.id.desc()
        )
        .limit((long) size + 1)
        .fetch();
  }

  @Override
  public List<IndexData> findForCsv(IndexDataSearchCondition condition) {

    String field = sortField(condition);
    boolean ascending = isAscending(condition);

    return queryFactory
        .selectFrom(data)
        .where(createFilter(condition))
        .orderBy(createOrder(field, ascending), ascending ? data.id.asc() : data.id.desc())
        .fetch();
  }

  // 검색 필터에 해당하는 전체 데이터 개수 조회
  @Override
  public long countByCondition(IndexDataSearchCondition condition) {
    Long count = queryFactory
        .select(data.id.count())
        .from(data)
        .where(createFilter(condition))
        .fetchOne();

    return count == null ? 0L : count;
  }

  // 지수 ID, 시작 날짜, 종료 날짜 필터 생성
  private BooleanBuilder createFilter(IndexDataSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();

    // 특정 지수만 조회
    if (condition.indexInfoId() != null) {
      builder.and(
          data.indexInfoId.eq(condition.indexInfoId())
      );
    }

    // 시작 날짜 이후의 데이터 조회
    if (condition.startDate() != null) {
      builder.and(
          data.baseDate.goe(condition.startDate())
      );
    }

    // 종료 날짜 이전의 데이터 조회
    if (condition.endDate() != null) {
      builder.and(
          data.baseDate.loe(condition.endDate())
      );
    }

    return builder;
  }

  // 정렬 필드가 없으면 baseDate를 기본값으로 사용
  private String sortField(IndexDataSearchCondition condition) {
    String field = condition.sortField();

    return field == null || field.isBlank()
        ? "baseDate"
        : field;
  }

  // A 파트와 동일한 정렬 방향 정책 적용함
  private boolean isAscending(IndexDataSearchCondition condition) {
    return CursorPaging.isAsc(condition.sortDirection());
  }

  // 프론트엔드에서 허용한 10개 필드에 대한 정렬 조건 생성
  private OrderSpecifier<?> createOrder(
      String field, boolean ascending
  ) {
    return switch (field) {
      case "baseDate" -> ascending ? data.baseDate.asc() : data.baseDate.desc();

      case "marketPrice" ->
          (ascending ? data.marketPrice.asc() : data.marketPrice.desc()).nullsLast();

      case "closingPrice" ->
          (ascending ? data.closingPrice.asc() : data.closingPrice.desc()).nullsLast();

      case "highPrice" -> (ascending ? data.highPrice.asc() : data.highPrice.desc()).nullsLast();

      case "lowPrice" -> (ascending ? data.lowPrice.asc() : data.lowPrice.desc()).nullsLast();

      case "versus" -> (ascending ? data.versus.asc() : data.versus.desc()).nullsLast();

      case "fluctuationRate" ->
          (ascending ? data.fluctuationRate.asc() : data.fluctuationRate.desc()).nullsLast();

      case "tradingQuantity" ->
          (ascending ? data.tradingQuantity.asc() : data.tradingQuantity.desc()).nullsLast();

      case "tradingPrice" ->
          (ascending ? data.tradingPrice.asc() : data.tradingPrice.desc()).nullsLast();

      case "marketTotalAmount" ->
          (ascending ? data.marketTotalAmount.asc() : data.marketTotalAmount.desc()).nullsLast();

      default -> throw new IllegalArgumentException(
          "지원하지 않는 정렬 필드: " + field
      );
    };
  }

  // 다음 페이지 조회를 위한 커서 조건 생성
  private BooleanExpression createCursorCondition(
      IndexDataSearchCondition condition,
      String field,
      boolean ascending
  ) {
    String cursor = condition.cursor();
    Long idAfter = condition.idAfter();

    boolean hasCursor = cursor != null && !cursor.isBlank();
    boolean hasIdAfter = idAfter != null;

    // 첫 페이지는 커서 조건을 적용하지 않음
    if (!hasCursor && !hasIdAfter) {
      return null;
    }

    // 정렬 값과 ID를 함께 사용해야 중복·누락을 방지할 수 있음
    if (!hasCursor || !hasIdAfter) {
      throw new IllegalArgumentException(
          "cursor와 idAfter를 함께 전달해야 합니다."
      );
    }

    return switch (field) {
      case "baseDate" -> dateCursor(LocalDate.parse(cursor), idAfter, ascending);

      case "marketPrice" ->
          numberCursor(data.marketPrice, decimalValue(cursor), idAfter, ascending);

      case "closingPrice" ->
          numberCursor(data.closingPrice, decimalValue(cursor), idAfter, ascending);

      case "highPrice" -> numberCursor(data.highPrice, decimalValue(cursor), idAfter, ascending);

      case "lowPrice" -> numberCursor(data.lowPrice, decimalValue(cursor), idAfter, ascending);

      case "versus" -> numberCursor(data.versus, decimalValue(cursor), idAfter, ascending);

      case "fluctuationRate" ->
          numberCursor(data.fluctuationRate, decimalValue(cursor), idAfter, ascending);

      case "tradingQuantity" ->
          numberCursor(data.tradingQuantity, longValue(cursor), idAfter, ascending);

      case "tradingPrice" -> numberCursor(data.tradingPrice, longValue(cursor), idAfter, ascending);

      case "marketTotalAmount" ->
          numberCursor(data.marketTotalAmount, longValue(cursor), idAfter, ascending);

      default -> throw new IllegalArgumentException(
          "지원하지 않는 정렬 필드: " + field
      );
    };
  }

  // 날짜 정렬 시 마지막 데이터 이후부터 조회
  private BooleanExpression dateCursor(
      LocalDate cursor, Long idAfter, boolean ascending
  ) {
    BooleanExpression next = ascending
        ? data.baseDate.gt(cursor)
        : data.baseDate.lt(cursor);

    BooleanExpression sameDate = data.baseDate.eq(cursor)
        .and(ascending
            ? data.id.gt(idAfter)
            : data.id.lt(idAfter));

    return next.or(sameDate);
  }

  // 숫자 정렬에서 null 값까지 포함해 다음 페이지 조회
  private <T extends Number & Comparable<?>> BooleanExpression numberCursor(
      NumberExpression<T> path,
      T cursor,
      Long idAfter,
      boolean ascending
  ) {

    // 정렬 방향에 맞춰 마지막 ID 이후 조건 생성
    BooleanExpression nextId = ascending
        ? data.id.gt(idAfter)
        : data.id.lt(idAfter);

    // 이전 페이지 마지막 정렬 값이 null인 경우
    if (cursor == null) {
      return path.isNull().and(nextId);
    }

    // 이전 정렬 값보다 뒤에 있는 데이터
    BooleanExpression next = ascending
        ? path.gt(cursor)
        : path.lt(cursor);

    // 정렬 값이 같다면 ID로 구분
    BooleanExpression sameValue = path.eq(cursor).and(nextId);

    // null 값은 정렬 방향과 상관없이 마지막에 배치
    return next
        .or(sameValue)
        .or(path.isNull());
  }

  // BigDecimal 커서 변환
  private BigDecimal decimalValue(String cursor) {
    return CursorPaging.NULL_CURSOR.equals(cursor)
        ? null
        : new BigDecimal(cursor);
  }

  // Long 커서 변환
  private Long longValue(String cursor) {
    return CursorPaging.NULL_CURSOR.equals(cursor)
        ? null
        : Long.valueOf(cursor);
  }
}

