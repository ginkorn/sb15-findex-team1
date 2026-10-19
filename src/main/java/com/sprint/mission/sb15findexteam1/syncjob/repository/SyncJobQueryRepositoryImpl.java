package com.sprint.mission.sb15findexteam1.syncjob.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.QSyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
    int size = CursorPaging.normalizeSize(condition.size());          // 크기가 없으면 10, 최대 100
    boolean asc = "asc".equalsIgnoreCase(condition.sortDirection());  // 방향이 없으면 내림차순(최신순)

    // 필터 조건에 커서 조건을 이어 붙인다 (첫 페이지는 커서가 없어서 null)
    BooleanBuilder builder = createFilter(condition);
    BooleanExpression cursorCondition = jobTimeCursor(condition, asc);
    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }

    return queryFactory
        .selectFrom(syncJob)
        .where(builder)
        .orderBy(CursorPaging.orderBy(syncJob.jobTime, syncJob.id, asc))   // 작업 일시 순서, 같으면 id 순서
        .limit((long) size + 1)                                            // 다음 페이지 확인용으로 1개 더
        .fetch();
  }

  // 작업 일시 정렬의 커서 조건: "이전 페이지 마지막 항목 바로 다음"만 남긴다
  private BooleanExpression jobTimeCursor(SyncJobSearchCondition condition, boolean asc) {
    String cursor = condition.cursor();
    Long idAfter = condition.idAfter();

    boolean hasCursor = cursor != null && !cursor.isBlank();
    if (!hasCursor && idAfter == null) {
      return null;                                  // 첫 페이지: 커서 조건 없음
    }
    if (!hasCursor || idAfter == null) {
      throw new IllegalArgumentException("cursor와 idAfter를 함께 전달해야 합니다.");   // 둘 중 하나만 오면 잘못된 요청
    }

    LocalDateTime cursorTime = LocalDateTime.parse(cursor);                 // "2025-01-15T11:00" 문자열을 시각으로
    return CursorPaging.after(syncJob.jobTime, cursorTime, syncJob.id, idAfter, asc);
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
