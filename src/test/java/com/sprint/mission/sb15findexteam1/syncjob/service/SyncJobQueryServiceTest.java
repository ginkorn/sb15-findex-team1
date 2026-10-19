package com.sprint.mission.sb15findexteam1.syncjob.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import com.sprint.mission.sb15findexteam1.syncjob.repository.SyncJobRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SyncJobQueryServiceTest {

  @Mock
  private SyncJobRepository syncJobRepository;      // 가짜 Repository (DB에 접속하지 않음)

  @InjectMocks
  private SyncJobQueryService service;              // 테스트 대상 (가짜 Repository가 주입됨)

  // 테스트용 이력을 만드는 도우미: DB가 없어서 id와 작업 일시를 직접 넣는다
  private SyncJob job(Long id, LocalDate targetDate, LocalDateTime jobTime) {
    SyncJob job = SyncJob.create(JobType.INDEX_DATA, null, targetDate, "worker", ResultType.SUCCESS);
    ReflectionTestUtils.setField(job, "id", id);
    ReflectionTestUtils.setField(job, "jobTime", jobTime);
    return job;
  }

  @Test
  @DisplayName("size보다 1개 더 조회되면 다음 페이지가 있다: 마지막 항목 기준으로 커서를 만든다")
  void getSyncJobs_hasNext() {
    // given: size 2인데 Repository가 3개(size + 1)를 돌려준다. 전체 개수는 5
    SyncJobSearchCondition condition = SyncJobSearchCondition.empty().withSize(2);
    given(syncJobRepository.findPageByCondition(condition)).willReturn(List.of(
        job(3L, LocalDate.of(2025, 1, 14), LocalDateTime.of(2025, 1, 15, 12, 0)),
        job(2L, LocalDate.of(2025, 1, 14), LocalDateTime.of(2025, 1, 15, 11, 0)),
        job(1L, LocalDate.of(2025, 1, 14), LocalDateTime.of(2025, 1, 15, 10, 0))));
    given(syncJobRepository.countByCondition(condition)).willReturn(5L);

    // when
    CursorPageResponseDto<SyncJobDto> result = service.getSyncJobs(condition);

    // then: 앞의 2개만 내용에 담기고, 다음 페이지가 있다
    assertThat(result.content()).hasSize(2);
    assertThat(result.content().get(0).id()).isEqualTo(3L);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.totalElements()).isEqualTo(5L);
    // 다음 커서 = 내용의 마지막 항목(id 2, 11시)의 정렬 값과 id
    assertThat(result.nextCursor()).isEqualTo(LocalDateTime.of(2025, 1, 15, 11, 0).toString());
    assertThat(result.nextIdAfter()).isEqualTo(2L);
  }

  @Test
  @DisplayName("마지막 페이지면 다음 커서가 없다")
  void getSyncJobs_lastPage() {
    // given: size 2인데 2개만 남아 있다
    SyncJobSearchCondition condition = SyncJobSearchCondition.empty().withSize(2);
    given(syncJobRepository.findPageByCondition(condition)).willReturn(List.of(
        job(2L, null, LocalDateTime.of(2025, 1, 15, 11, 0)),
        job(1L, null, LocalDateTime.of(2025, 1, 15, 10, 0))));
    given(syncJobRepository.countByCondition(condition)).willReturn(2L);

    CursorPageResponseDto<SyncJobDto> result = service.getSyncJobs(condition);

    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextIdAfter()).isNull();
  }

  @Test
  @DisplayName("대상 날짜 정렬이면 다음 커서가 마지막 항목의 대상 날짜다 (없으면 __NULL__)")
  void getSyncJobs_targetDateCursor() {
    // given: 대상 날짜 정렬, size 1, 2개가 조회됨. 첫 번째의 대상 날짜가 null
    SyncJobSearchCondition condition = new SyncJobSearchCondition(null, null, null, null, null, null, null, null,
        null, null, "targetDate", "asc", 1);
    given(syncJobRepository.findPageByCondition(condition)).willReturn(List.of(
        job(1L, null, LocalDateTime.of(2025, 1, 15, 10, 0)),
        job(2L, LocalDate.of(2025, 1, 14), LocalDateTime.of(2025, 1, 15, 11, 0))));
    given(syncJobRepository.countByCondition(condition)).willReturn(2L);

    CursorPageResponseDto<SyncJobDto> result = service.getSyncJobs(condition);

    // then: 내용 1개(대상 날짜 null), 다음 커서는 "__NULL__"
    assertThat(result.content()).hasSize(1);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo("__NULL__");
    assertThat(result.nextIdAfter()).isEqualTo(1L);
  }

  @Test
  @DisplayName("조회 결과가 없으면 빈 내용으로 응답한다")
  void getSyncJobs_empty() {
    SyncJobSearchCondition condition = SyncJobSearchCondition.empty();
    given(syncJobRepository.findPageByCondition(condition)).willReturn(List.of());
    given(syncJobRepository.countByCondition(condition)).willReturn(0L);

    CursorPageResponseDto<SyncJobDto> result = service.getSyncJobs(condition);

    assertThat(result.content()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    assertThat(result.totalElements()).isZero();
  }
}