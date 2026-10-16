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
 *
 * <p>응답에 지수명이 필요해서 지수 정보를 fetch join으로 한 번에 가져온다.
 * (따로 가져오면 설정 10개 + 지수 10번 = 쿼리 11번이 나가는 N+1 문제가 생긴다)
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

  @Override
  public long countByCondition(AutoSyncConfigSearchCondition condition) {
    Long total = queryFactory.select(config.count())
        .from(config)
        .where(indexInfoIdEq(condition.indexInfoId()), enabledEq(condition.enabled()))
        .fetchOne();
    return total == null ? 0 : total;
  }

  private BooleanExpression indexInfoIdEq(Long indexInfoId) {
    return indexInfoId != null ? config.indexInfo.id.eq(indexInfoId) : null;
  }

  private BooleanExpression enabledEq(Boolean enabled) {
    return enabled != null ? config.enabled.eq(enabled) : null;
  }

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

  private OrderSpecifier<?>[] orderBy(AutoSyncConfigSearchCondition condition) {
    boolean asc = condition.isAsc();
    if (condition.sortByEnabled()) {
      return CursorPaging.orderBy(config.enabled, config.id, asc);
    }
    return CursorPaging.orderBy(indexInfo.indexName, config.id, asc);
  }
}
