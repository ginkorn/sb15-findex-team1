package com.sprint.mission.sb15findexteam1.syncjob.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.QSyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SyncJobQueryRepositoryImpl implements SyncJobQueryRepository{

  private static final QSyncJob syncJob = QSyncJob.syncJob;
  private final JPAQueryFactory queryFactory;

  public SyncJobQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @Override
  public List<SyncJob> findPageByCondition(SyncJobSearchCondition condition) {
    int size = CursorPaging.normalizeSize(condition.size());    // 크기가 없으면 10, 최대 100
    boolean asc = "asc".equalsIgnoreCase(condition.sortDirection());   // 방향이 없으면 내림차순(최신순)

    return queryFactory
        .selectFrom(syncJob)                                    // sync_job 테이블에서 조회
        .where(createFilter(condition))                         // 검색 조건 (지금은 비어 있음)
        .orderBy(CursorPaging.orderBy(syncJob.jobTime, syncJob.id, asc))   // 작업 일시 순서, 같으면 id 순서
        .limit((long) size + 1)                                 // 다음 페이지가 있는지 알려고 1개 더 조회
        .fetch();
  }

  @Override
  public long countByCondition(SyncJobSearchCondition condition) {
    Long count = queryFactory
        .select(syncJob.id.count())                             // 개수만 센다
        .from(syncJob)
        .where(createFilter(condition))                         // 커서를 뺀 필터 조건만 적용
        .fetchOne();
    return count == null ? 0L : count;
  }

  // 검색 조건에서 값이 있는 것만 WHERE 조건으로 쌓는다 (필터는 다음 단계에서 하나씩 추가)
  private BooleanBuilder createFilter(SyncJobSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();
    // 작업 유형 (INDEX_INFO / INDEX_DATA)
    if (condition.jobType() != null) {
      builder.and(syncJob.jobType.eq(condition.jobType()));
    }
    // 지수: 이력에 연결된 지수의 id가 같은 것
    if (condition.indexInfoId() != null) {
      builder.and(syncJob.indexInfo.id.eq(condition.indexInfoId()));
    }
    // 작업자: 일부만 입력해도 찾는다 (대소문자 구분 없음)
    if (condition.worker() != null && !condition.worker().isBlank()) {
      builder.and(syncJob.worker.containsIgnoreCase(condition.worker().trim()));
    }
    // 결과 (SUCCESS / FAILED)
    if (condition.status() != null) {
      builder.and(syncJob.result.eq(condition.status()));
    }
    if (condition.jobTimeFrom() != null) {
      builder.and(syncJob.jobTime.goe(condition.jobTimeFrom()));    // 시작 시각 이상
    }
    if (condition.jobTimeTo() != null) {
      builder.and(syncJob.jobTime.loe(condition.jobTimeTo()));      // 끝 시각 이하
    }
    return builder;
  }
}
