
package com.sprint.mission.sb15findexteam1.indexdata.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.entity.QIndexData;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class IndexDataQueryRepositoryImpl
    implements IndexDataQueryRepository {

  // QueryDSL이 자동 생성한 지수 데이터 조회용 클래스
  private static final QIndexData data = QIndexData.indexData;

  // JPA를 이용해 동적 쿼리를 생성하는 객체
  private final JPAQueryFactory queryFactory;

  // EntityManager를 주입받아 QueryDSL 사용 준비
  public IndexDataQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  // 검색 조건 + 정렬 + 커서를 적용하여 한 페이지 조회
  @Override
  public List<IndexData> findPageByCondition(
      IndexDataSearchCondition condition
  ) {
    String sortField = sortField(condition);
    boolean ascending = isAscending(condition);
    int size = condition.size() == null ? 10 : condition.size();

    // 조회 개수는 1개 이상이어야 함
    if (size < 1) {
      throw new IllegalArgumentException(
          "size는 1 이상이어야 합니다."
      );
    }

    // 지수 ID와 날짜 조건 생성
    BooleanBuilder builder = createFilter(condition);

    // 이전 페이지의 마지막 데이터 이후부터 조회
    BooleanExpression cursorCondition =
        createCursorCondition(condition, sortField, ascending);

    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }

    // TODO(A): 공통 페이지 응답 DTO 연결은 Service에서 처리
    // 다음 페이지 여부 판단을 위해 size보다 1개 더 조회
    return queryFactory
        .selectFrom(data)
        .where(builder)
        .orderBy(
            createOrder(sortField, ascending),
            data.id.asc()
        )
        .limit((long) size + 1)
        .fetch();
  }

  // CSV 다운로드에 사용할 전체 데이터 조회
  @Override
  public List<IndexData> findForCsv(
      IndexDataSearchCondition condition
  ) {
    return queryFactory
        .selectFrom(data)
        .where(createFilter(condition))
        .orderBy(
            createOrder(sortField(condition), isAscending(condition)),
            data.id.asc()
        )
        .fetch();
  }

  // 검색 필터에 해당하는 전체 데이터 개수 조회
  @Override
  public long countByCondition(
      IndexDataSearchCondition condition
  ) {
    Long count = queryFactory
        .select(data.id.count())
        .from(data)
        .where(createFilter(condition))
        .fetchOne();

    return count == null ? 0L : count;
  }

  // 지수 ID, 시작 날짜, 종료 날짜 필터 생성
  private BooleanBuilder createFilter(
      IndexDataSearchCondition condition
  ) {
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

  // 정렬 방향이 없으면 내림차순 사용
  private boolean isAscending(IndexDataSearchCondition condition) {
    String direction = condition.sortDirection();

    if (direction == null || direction.isBlank()) {
      return false;
    }

    if (!direction.equalsIgnoreCase("asc")
        && !direction.equalsIgnoreCase("desc")) {
      throw new IllegalArgumentException("올바르지 않은 정렬 방향입니다.");
    }

    return direction.equalsIgnoreCase("asc");
  }

  // 프론트엔드에서 허용한 10개 필드에 대한 정렬 조건 생성
  private OrderSpecifier<?> createOrder(
      String field, boolean ascending
  ) {
    return switch (field) {
      case "baseDate" ->
          ascending ? data.baseDate.asc() : data.baseDate.desc();

      case "marketPrice" ->
          (ascending ? data.marketPrice.asc() : data.marketPrice.desc()).nullsLast();

      case "closingPrice" ->
          (ascending ? data.closingPrice.asc() : data.closingPrice.desc()).nullsLast();

      case "highPrice" ->
          (ascending ? data.highPrice.asc() : data.highPrice.desc()).nullsLast();

      case "lowPrice" ->
          (ascending ? data.lowPrice.asc() : data.lowPrice.desc()).nullsLast();

      case "versus" ->
          (ascending ? data.versus.asc() : data.versus.desc()).nullsLast();

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
      case "baseDate" ->
          dateCursor(LocalDate.parse(cursor), idAfter, ascending);

      case "marketPrice" ->
          numberCursor(data.marketPrice, decimalValue(cursor), idAfter, ascending);

      case "closingPrice" ->
          numberCursor(data.closingPrice, decimalValue(cursor), idAfter, ascending);

      case "highPrice" ->
          numberCursor(data.highPrice, decimalValue(cursor), idAfter, ascending);

      case "lowPrice" ->
          numberCursor(data.lowPrice, decimalValue(cursor), idAfter, ascending);

      case "versus" ->
          numberCursor(data.versus, decimalValue(cursor), idAfter, ascending);

      case "fluctuationRate" ->
          numberCursor(data.fluctuationRate, decimalValue(cursor), idAfter, ascending);

      case "tradingQuantity" ->
          numberCursor(data.tradingQuantity, longValue(cursor), idAfter, ascending);

      case "tradingPrice" ->
          numberCursor(data.tradingPrice, longValue(cursor), idAfter, ascending);

      case "marketTotalAmount" ->
          numberCursor(data.marketTotalAmount, longValue(cursor), idAfter, ascending);

      default -> throw new IllegalArgumentException(
          "지원하지 않는 정렬 필드: " + field
      );
    };
  }

  // 날짜 정렬에서 마지막으로 조회한 데이터 이후를 검색
  private BooleanExpression dateCursor(
      LocalDate cursor, Long idAfter, boolean ascending
  ) {
    BooleanExpression next = ascending
        ? data.baseDate.gt(cursor)
        : data.baseDate.lt(cursor);

    return next.or(
        data.baseDate.eq(cursor).and(data.id.gt(idAfter))
    );
  }

  // 숫자 정렬에서 커서 값과 ID를 이용해 다음 데이터를 검색
  private <T extends Number & Comparable<?>> BooleanExpression numberCursor(
      NumberExpression<T> path,
      T cursor,
      Long idAfter,
      boolean ascending
  ) {
    // 정렬 값이 null인 데이터도 페이지네이션 가능하도록 처리
    if (cursor == null) {
      return path.isNull().and(data.id.gt(idAfter));
    }

    BooleanExpression next = ascending
        ? path.gt(cursor)
        : path.lt(cursor);

    return next
        .or(path.eq(cursor).and(data.id.gt(idAfter)))
        .or(path.isNull());
  }

  // BigDecimal 커서 문자열 변환
  private BigDecimal decimalValue(String cursor) {
    return "__NULL__".equals(cursor)
        ? null
        : new BigDecimal(cursor);
  }

  // Long 커서 문자열 변환
  private Long longValue(String cursor) {
    return "__NULL__".equals(cursor)
        ? null
        : Long.valueOf(cursor);
  }

  // TODO(A): 페이지 응답 조립 시 마지막 행의 정렬 값을
  // nextCursor로 변환하고, 마지막 행의 id를 nextIdAfter로 전달
  //
  // TODO(A): 정렬 값이 null인 경우 "__NULL__"로 전달하도록 협의
  //
  // TODO(A): 기본 size, 최대 size 등 공통 정책 최종 확인
}
