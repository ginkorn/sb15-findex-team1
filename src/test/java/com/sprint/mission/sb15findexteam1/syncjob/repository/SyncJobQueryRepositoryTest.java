package com.sprint.mission.sb15findexteam1.syncjob.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobQueryRepositoryTest {

  @Autowired
  private SyncJobRepository repository;

  @Autowired
  private EntityManager entityManager;

  // 테스트용 연동 이력을 저장하는 도우미 (작업 일시를 직접 정한다)
  // create()는 jobTime을 항상 now()로 넣어서, 원하는 시각으로 덮어쓴다
  private SyncJob job(String worker, LocalDateTime jobTime) {
    SyncJob job = SyncJob.create(JobType.INDEX_DATA, null, LocalDate.of(2025, 1, 14), worker, ResultType.SUCCESS);
    ReflectionTestUtils.setField(job, "jobTime", jobTime);
    entityManager.persist(job);
    return job;
  }

  // 지수와 작업 유형, 결과까지 정해서 이력을 저장하는 도우미
  private SyncJob job(JobType type, IndexInfo indexInfo, String worker, ResultType result, LocalDateTime jobTime) {
    SyncJob job = SyncJob.create(type, indexInfo, LocalDate.of(2025, 1, 14), worker, result);
    ReflectionTestUtils.setField(job, "jobTime", jobTime);
    entityManager.persist(job);
    return job;
  }

  // 테스트용 지수 정보를 저장하는 도우미 (이름만 의미 있음)
  private IndexInfo indexInfo(String name) {
    IndexInfo info = IndexInfo.create("테스트분류", name, 100, LocalDate.of(2000, 1, 1),
        new BigDecimal("100"), SourceType.USER, false);
    entityManager.persist(info);
    return info;
  }

  // 필터 4개만 지정하는 검색 조건 도우미 (나머지는 null)
  private SyncJobSearchCondition condition(JobType jobType, Long indexInfoId, String worker, ResultType status) {
    return new SyncJobSearchCondition(jobType, indexInfoId, null, null, worker, null, null, status,
        null, null, null, null, null);
  }

  @Test
  @DisplayName("[QueryDSL] 조건이 없으면 작업 일시 최신순으로 조회한다")
  void findPage_defaultOrder() {
    // given: 작업 일시가 서로 다른 이력 3개 (저장 순서와 시각 순서를 일부러 섞음)
    job("b", LocalDateTime.of(2025, 1, 15, 10, 0));
    job("a", LocalDateTime.of(2025, 1, 15, 9, 0));
    job("c", LocalDateTime.of(2025, 1, 15, 11, 0));

    // when
    List<SyncJob> result = repository.findPageByCondition(SyncJobSearchCondition.empty());

    // then: 작업 일시가 가장 늦은 것부터 (c, b, a)
    assertThat(result).extracting(SyncJob::getWorker).containsExactly("c", "b", "a");
  }

  @Test
  @DisplayName("[QueryDSL] size가 2면 다음 페이지 확인을 위해 3개까지 조회한다")
  void findPage_sizePlusOne() {
    // given: 이력 3개
    job("a", LocalDateTime.of(2025, 1, 15, 9, 0));
    job("b", LocalDateTime.of(2025, 1, 15, 10, 0));
    job("c", LocalDateTime.of(2025, 1, 15, 11, 0));

    // when: size 2
    List<SyncJob> result = repository.findPageByCondition(SyncJobSearchCondition.empty().withSize(2));

    // then: 2개가 아니라 3개(size + 1)가 나온다. 3번째가 있으면 "다음 페이지가 있다"는 뜻
    assertThat(result).hasSize(3);
  }

  private static final LocalDateTime T = LocalDateTime.of(2025, 1, 15, 10, 0);   // 시각은 의미 없어서 하나로 통일

  @Test
  @DisplayName("[QueryDSL] 결과(status)로 거른다")
  void filter_status() {
    job(JobType.INDEX_DATA, null, "a", ResultType.SUCCESS, T);
    job(JobType.INDEX_DATA, null, "b", ResultType.FAILED, T);

    List<SyncJob> result = repository.findPageByCondition(condition(null, null, null, ResultType.FAILED));

    assertThat(result).extracting(SyncJob::getWorker).containsExactly("b");
  }

  @Test
  @DisplayName("[QueryDSL] 작업 유형(jobType)으로 거른다")
  void filter_jobType() {
    job(JobType.INDEX_INFO, null, "info", ResultType.SUCCESS, T);
    job(JobType.INDEX_DATA, null, "data", ResultType.SUCCESS, T);

    List<SyncJob> result = repository.findPageByCondition(condition(JobType.INDEX_INFO, null, null, null));

    assertThat(result).extracting(SyncJob::getWorker).containsExactly("info");
  }

  @Test
  @DisplayName("[QueryDSL] 작업자(worker)는 일부만 입력해도 찾는다")
  void filter_worker() {
    job(JobType.INDEX_DATA, null, "192.168.0.5", ResultType.SUCCESS, T);
    job(JobType.INDEX_DATA, null, "system", ResultType.SUCCESS, T);

    List<SyncJob> result = repository.findPageByCondition(condition(null, null, "192.168", null));

    assertThat(result).extracting(SyncJob::getWorker).containsExactly("192.168.0.5");
  }

  @Test
  @DisplayName("[QueryDSL] 지수(indexInfoId)로 거른다")
  void filter_indexInfoId() {
    IndexInfo kospi = indexInfo("코스피");
    IndexInfo kosdaq = indexInfo("코스닥");
    job(JobType.INDEX_DATA, kospi, "kospi-job", ResultType.SUCCESS, T);
    job(JobType.INDEX_DATA, kosdaq, "kosdaq-job", ResultType.SUCCESS, T);

    List<SyncJob> result = repository.findPageByCondition(condition(null, kospi.getId(), null, null));

    assertThat(result).extracting(SyncJob::getWorker).containsExactly("kospi-job");
  }


  // 작업 일시 범위만 지정하는 검색 조건 도우미 (나머지는 null)
  private SyncJobSearchCondition timeCondition(LocalDateTime from, LocalDateTime to) {
    return new SyncJobSearchCondition(null, null, null, null, null, from, to, null,
        null, null, null, null, null);
  }

  @Test
  @DisplayName("[QueryDSL] 작업 일시 범위(jobTimeFrom ~ jobTimeTo)로 거른다 (양 끝 포함)")
  void filter_jobTimeRange() {
    // given: 9시, 10시, 11시, 12시 이력 4개
    job(JobType.INDEX_DATA, null, "t9", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 9, 0));
    job(JobType.INDEX_DATA, null, "t10", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 10, 0));
    job(JobType.INDEX_DATA, null, "t11", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 11, 0));
    job(JobType.INDEX_DATA, null, "t12", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 12, 0));

    // when: 10시 ~ 11시
    List<SyncJob> result = repository.findPageByCondition(timeCondition(
        LocalDateTime.of(2025, 1, 15, 10, 0), LocalDateTime.of(2025, 1, 15, 11, 0)));

    // then: 양 끝(10시, 11시)을 포함해서 최신순으로 t11, t10
    assertThat(result).extracting(SyncJob::getWorker).containsExactly("t11", "t10");
  }

  @Test
  @DisplayName("[QueryDSL] jobTimeFrom만 주면 그 시각 이후만 조회한다")
  void filter_jobTimeFromOnly() {
    job(JobType.INDEX_DATA, null, "t9", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 9, 0));
    job(JobType.INDEX_DATA, null, "t12", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 12, 0));

    List<SyncJob> result = repository.findPageByCondition(timeCondition(LocalDateTime.of(2025, 1, 15, 10, 0), null));

    assertThat(result).extracting(SyncJob::getWorker).containsExactly("t12");
  }

  // 정렬, 커서, 크기를 지정하는 검색 조건 도우미 (필터는 모두 null)
  private SyncJobSearchCondition pageCondition(String sortField, String sortDirection,
      String cursor, Long idAfter, Integer size) {
    return new SyncJobSearchCondition(null, null, null, null, null, null, null, null,
        idAfter, cursor, sortField, sortDirection, size);
  }

  @Test
  @DisplayName("[QueryDSL] 커서를 주면 그 항목 바로 다음부터 조회한다 (작업 일시 최신순)")
  void cursor_jobTime() {
    // given: 9시, 10시, 11시, 12시 이력
    job(JobType.INDEX_DATA, null, "t9", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 9, 0));
    job(JobType.INDEX_DATA, null, "t10", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 10, 0));
    SyncJob t11 = job(JobType.INDEX_DATA, null, "t11", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 11, 0));
    job(JobType.INDEX_DATA, null, "t12", ResultType.SUCCESS, LocalDateTime.of(2025, 1, 15, 12, 0));

    // when: 1페이지(size 2)의 마지막이었던 t11을 커서로 다음 페이지를 요청
    List<SyncJob> result = repository.findPageByCondition(
        pageCondition("jobTime", "desc", t11.getJobTime().toString(), t11.getId(), 2));

    // then: t11 다음부터 t10, t9
    assertThat(result).extracting(SyncJob::getWorker).containsExactly("t10", "t9");
  }

  @Test
  @DisplayName("[QueryDSL] 작업 일시가 같으면 id로 구분해서 다음부터 조회한다")
  void cursor_jobTime_tieBreak() {
    // given: 작업 일시가 똑같은 이력 3개 (id 순서대로 a, b, c)
    LocalDateTime same = LocalDateTime.of(2025, 1, 15, 10, 0);
    job(JobType.INDEX_DATA, null, "a", ResultType.SUCCESS, same);
    SyncJob b = job(JobType.INDEX_DATA, null, "b", ResultType.SUCCESS, same);
    job(JobType.INDEX_DATA, null, "c", ResultType.SUCCESS, same);

    // when: 최신순(id가 큰 쪽이 앞)이라 c, b, a 순서. b를 커서로 다음 페이지 요청
    List<SyncJob> result = repository.findPageByCondition(
        pageCondition("jobTime", "desc", same.toString(), b.getId(), 10));

    // then: b 다음은 a 하나뿐
    assertThat(result).extracting(SyncJob::getWorker).containsExactly("a");
  }
}