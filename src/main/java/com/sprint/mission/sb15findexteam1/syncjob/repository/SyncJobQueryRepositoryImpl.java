package com.sprint.mission.sb15findexteam1.syncjob.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.QSyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SyncJobQueryRepositoryImpl implements SyncJobQueryRepository{

  private static final QSyncJob syncJob = QSyncJob.syncJob;
  private final JPAQueryFactory queryFactory;

  // 대상 날짜가 null인 이력은 정렬할 때 이 날짜(가장 오래된 날짜)로 취급한다
  private static final LocalDate NULL_DATE = LocalDate.of(1970, 1, 1);

  public SyncJobQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @Override
  public List<SyncJob> findPageByCondition(SyncJobSearchCondition condition) {
    int size = CursorPaging.normalizeSize(condition.size());          // 크기가 없으면 10, 최대 100
    boolean asc = "asc".equalsIgnoreCase(condition.sortDirection());  // 방향이 없으면 내림차순(최신순)
    boolean byTargetDate = isTargetDateSort(condition);               // 대상 날짜 정렬인지 (아니면 작업 일시)

    // 필터 조건에 커서 조건을 이어 붙인다 (첫 페이지는 커서가 없어서 null)
    BooleanBuilder builder = createFilter(condition);
    BooleanExpression cursorCondition = byTargetDate
        ? targetDateCursor(condition, asc)
        : jobTimeCursor(condition, asc);
    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }

    // 정렬 기준: 선택한 필드 → 같으면 id
    OrderSpecifier<?>[] order = byTargetDate
        ? CursorPaging.orderBy(targetDateOrMin(), syncJob.id, asc)
        : CursorPaging.orderBy(syncJob.jobTime, syncJob.id, asc);

    return queryFactory
        .selectFrom(syncJob)
        .where(builder)
        .orderBy(order)
        .limit((long) size + 1)                                       // 다음 페이지 확인용으로 1개 더
        .fetch();
  }

  // 정렬 필드가 대상 날짜(targetDate)인지 판단한다. 비어 있거나 jobTime이면 작업 일시 정렬
  private boolean isTargetDateSort(SyncJobSearchCondition condition) {
    String field = condition.sortField();
    if (field == null || field.isBlank() || "jobTime".equals(field)) {
      return false;
    }
    if ("targetDate".equals(field)) {
      return true;
    }
    throw new IllegalArgumentException("지원하지 않는 정렬 필드: " + field);
  }

  // 대상 날짜가 null이면 NULL_DATE로 바꿔서 정렬, 비교에 쓴다 (DB 종류와 상관없이 항상 같은 순서)
  private DateExpression<LocalDate> targetDateOrMin() {
    return syncJob.targetDate.coalesce(NULL_DATE);
  }

  // 커서가 있는지 확인한다. 한쪽만 오면 잘못된 요청이라 예외를 던진다
  private boolean hasCursor(SyncJobSearchCondition condition) {
    boolean hasCursor = condition.cursor() != null && !condition.cursor().isBlank();
    boolean hasIdAfter = condition.idAfter() != null;
    if (!hasCursor && !hasIdAfter) {
      return false;                                  // 첫 페이지: 커서 조건 없음
    }
    if (!hasCursor || !hasIdAfter) {
      throw new IllegalArgumentException("cursor와 idAfter를 함께 전달해야 합니다.");
    }
    return true;
  }

  // 작업 일시 정렬의 커서 조건
  private BooleanExpression jobTimeCursor(SyncJobSearchCondition condition, boolean asc) {
    if (!hasCursor(condition)) {
      return null;
    }
    LocalDateTime cursorTime = LocalDateTime.parse(condition.cursor());
    return CursorPaging.after(syncJob.jobTime, cursorTime, syncJob.id, condition.idAfter(), asc);
  }

  // 대상 날짜 정렬의 커서 조건 ("__NULL__" 커서는 NULL_DATE로 해석)
  private BooleanExpression targetDateCursor(SyncJobSearchCondition condition, boolean asc) {
    if (!hasCursor(condition)) {
      return null;
    }
    LocalDate cursorDate = CursorPaging.NULL_CURSOR.equals(condition.cursor())
        ? NULL_DATE
        : LocalDate.parse(condition.cursor());
    return CursorPaging.after(targetDateOrMin(), cursorDate, syncJob.id, condition.idAfter(), asc);
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
