package com.sprint.mission.sb15findexteam1.syncjob.service;

import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.repository.SyncJobRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 연동 이력 목록 조회 서비스 (C의 SyncJobService는 건드리지 않고 조회 전용으로 따로 둔다)
@Service
@RequiredArgsConstructor
public class SyncJobQueryService {

  private final SyncJobRepository syncJobRepository;

  @Transactional(readOnly = true)
  public CursorPageResponseDto<SyncJobDto> getSyncJobs(SyncJobSearchCondition condition) {
    int size = CursorPaging.normalizeSize(condition.size());          // 크기가 없으면 10, 최대 100
    boolean byTargetDate = "targetDate".equals(condition.sortField());   // 정렬 기준에 따라 커서 값이 달라진다

    // 1) Repository가 size + 1개까지 조회한 이력을 응답용 DTO로 바꾼다
    List<SyncJobDto> rows = syncJobRepository.findPageByCondition(condition).stream()
        .map(SyncJobDto::toSyncJobDto)                // C가 만든 변환 메서드를 그대로 사용
        .toList();

    // 2) 커서를 뺀 필터 조건만으로 전체 개수를 센다
    long total = syncJobRepository.countByCondition(condition);

    // 3) hasNext, nextCursor, nextIdAfter를 계산해서 페이지 응답을 만든다
    return CursorPageResponseDto.of(
        rows, size, total,
        dto -> byTargetDate ? dto.targetDate() : dto.jobTime(),     // 정렬 기준 값을 꺼내는 방법
        SyncJobDto::id);                                            // id를 꺼내는 방법
  }
}