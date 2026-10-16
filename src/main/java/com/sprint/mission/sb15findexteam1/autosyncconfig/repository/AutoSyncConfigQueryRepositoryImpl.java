package com.sprint.mission.sb15findexteam1.autosyncconfig.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigSearchCondition;
import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.QAutoSyncConfig;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.QIndexInfo;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * 자동 연동 설정 목록 검색 구현 (QueryDSL).
 * 기본 원리(동적 조건, 커서, 정렬)는 IndexInfoQueryRepositoryImpl과 같다.
 *
 * <p><b>fetch join을 쓰는 이유 (N+1 문제)</b>
 * 응답에 지수명이 필요한데, AutoSyncConfig는 지수 정보를 "필요할 때 가져오기(LAZY)"로 가지고 있다.
 * 그냥 조회하면 설정 10개를 가져온 뒤 지수명을 볼 때마다 쿼리가 1번씩 더 나가서 총 11번(1 + N)이 된다.
 * {@code .join(...).fetchJoin()}을 쓰면 설정과 지수 정보를 쿼리 1번으로 함께 가져온다.
 */
public class AutoSyncConfigQueryRepositoryImpl implements AutoSyncConfigQueryRepository {

  private static final QAutoSyncConfig config = QAutoSyncConfig.autoSyncConfig;
  private static final QIndexInfo indexInfo = QIndexInfo.indexInfo;

  private final JPAQueryFactory queryFactory;

  // EntityManager로 직접 만든다. (@DataJpaTest처럼 JPA 빈만 띄우는 테스트에서도 동작하도록,
  //  B·D의 QueryRepository와 같은 방식)
  public AutoSyncConfigQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 한 페이지 조회 (지수 정보 함께 가져오기, size + 1개) */
  @Override
  public List<AutoSyncConfig> findPageByCondition(AutoSyncConfigSearchCondition condition) {
    return queryFactory.selectFrom(config)
        .join(config.indexInfo, indexInfo).fetchJoin()
        .where(
            indexInfoIdEq(condition.indexInfoId()),
            enabledEq(condition.enabled()),
            cursorCondition(condition)
        )
        .orderBy(orderBy(condition))
        .limit(condition.size() + 1L)
        .fetch();
  }

  /** 전체 개수 (필터만 적용, 커서 제외) */
  @Override
  public long countByCondition(AutoSyncConfigSearchCondition condition) {
    Long total = queryFactory.select(config.count())
        .from(config)
        .where(indexInfoIdEq(condition.indexInfoId()), enabledEq(condition.enabled()))
        .fetchOne();
    return total == null ? 0 : total;
  }

  /** 특정 지수의 설정만 */
  private BooleanExpression indexInfoIdEq(Long indexInfoId) {
    return indexInfoId != null ? config.indexInfo.id.eq(indexInfoId) : null;
  }

  /** 켜진 것만(true) / 꺼진 것만(false) */
  private BooleanExpression enabledEq(Boolean enabled) {
    return enabled != null ? config.enabled.eq(enabled) : null;
  }

  /** 다음 페이지 조건. 활성화 정렬이면 cursor "true"/"false"를 boolean으로 바꿔서 비교한다. */
  private BooleanExpression cursorCondition(AutoSyncConfigSearchCondition condition) {
    if (condition.cursor() == null || condition.idAfter() == null) {
      return null; // 첫 페이지
    }
    boolean asc = condition.isAsc();
    if (condition.sortByEnabled()) {
      return CursorPaging.after(config.enabled, Boolean.valueOf(condition.cursor()),
          config.id, condition.idAfter(), asc);
    }
    return CursorPaging.after(indexInfo.indexName, condition.cursor(), config.id, condition.idAfter(), asc);
  }

  /** 정렬: 지수명(기본) 또는 활성화 여부 → id */
  private OrderSpecifier<?>[] orderBy(AutoSyncConfigSearchCondition condition) {
    boolean asc = condition.isAsc();
    if (condition.sortByEnabled()) {
      return CursorPaging.orderBy(config.enabled, config.id, asc);
    }
    return CursorPaging.orderBy(indexInfo.indexName, config.id, asc);
  }
}
