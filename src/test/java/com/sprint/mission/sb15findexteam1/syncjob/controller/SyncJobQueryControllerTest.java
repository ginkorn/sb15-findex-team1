package com.sprint.mission.sb15findexteam1.syncjob.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import com.sprint.mission.sb15findexteam1.syncjob.service.SyncJobQueryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SyncJobQueryController.class)
class SyncJobQueryControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  SyncJobQueryService syncJobQueryService;       // 가짜 서비스

  @Test
  @DisplayName("연동 이력 목록: 서비스 결과를 커서 페이지 JSON으로 응답한다")
  void getSyncJobs() throws Exception {
    // given: 서비스가 이력 1건이 담긴 페이지를 돌려준다고 가정
    SyncJobDto dto = new SyncJobDto(1L, JobType.INDEX_DATA, 5L, LocalDate.of(2025, 1, 14),
        "system", LocalDateTime.of(2025, 1, 15, 10, 0), ResultType.SUCCESS);
    given(syncJobQueryService.getSyncJobs(any()))
        .willReturn(new CursorPageResponseDto<>(List.of(dto), "2025-01-15T10:00", 1L, 1, 1L, true));

    // when & then
    mockMvc.perform(get("/api/sync-jobs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].jobType").value("INDEX_DATA"))
        .andExpect(jsonPath("$.content[0].indexInfoId").value(5))
        .andExpect(jsonPath("$.content[0].worker").value("system"))
        .andExpect(jsonPath("$.content[0].result").value("SUCCESS"))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.nextCursor").value("2025-01-15T10:00"))
        .andExpect(jsonPath("$.nextIdAfter").value(1))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("연동 이력 목록: 쿼리 파라미터가 검색 조건으로 서비스에 전달된다")
  void getSyncJobs_binding() throws Exception {
    // given: 어떤 조건으로 불려도 빈 페이지를 돌려준다
    given(syncJobQueryService.getSyncJobs(any()))
        .willReturn(new CursorPageResponseDto<>(List.of(), null, null, 0, 0L, false));

    // when: 화면이 보내는 형태의 파라미터로 호출 (작업 일시는 'T'가 들어간 형식)
    mockMvc.perform(get("/api/sync-jobs")
            .param("jobType", "INDEX_DATA")
            .param("indexInfoId", "5")
            .param("worker", "system")
            .param("status", "FAILED")
            .param("jobTimeFrom", "2025-01-15T10:00:00")
            .param("jobTimeTo", "2025-01-15T18:30:00")
            .param("sortField", "jobTime")
            .param("sortDirection", "desc")
            .param("idAfter", "7")
            .param("cursor", "2025-01-15T11:00")
            .param("size", "5"))
        .andExpect(status().isOk());

    // then: 서비스가 받은 검색 조건을 꺼내서 값이 올바르게 들어왔는지 확인
    ArgumentCaptor<SyncJobSearchCondition> captor = ArgumentCaptor.forClass(SyncJobSearchCondition.class);
    verify(syncJobQueryService).getSyncJobs(captor.capture());
    SyncJobSearchCondition condition = captor.getValue();
    assertThat(condition.jobType()).isEqualTo(JobType.INDEX_DATA);
    assertThat(condition.indexInfoId()).isEqualTo(5L);
    assertThat(condition.worker()).isEqualTo("system");
    assertThat(condition.status()).isEqualTo(ResultType.FAILED);
    assertThat(condition.jobTimeFrom()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 0, 0));
    assertThat(condition.jobTimeTo()).isEqualTo(LocalDateTime.of(2025, 1, 15, 18, 30, 0));
    assertThat(condition.sortField()).isEqualTo("jobTime");
    assertThat(condition.idAfter()).isEqualTo(7L);
    assertThat(condition.cursor()).isEqualTo("2025-01-15T11:00");
    assertThat(condition.size()).isEqualTo(5);
  }

  @Test
  @DisplayName("연동 이력 목록: 빈 문자열 날짜(필터 해제)는 조건 없음으로 처리한다")
  void getSyncJobs_blankDate() throws Exception {
    given(syncJobQueryService.getSyncJobs(any()))
        .willReturn(new CursorPageResponseDto<>(List.of(), null, null, 0, 0L, false));

    // 화면에서 날짜 필터를 취소하면 jobTimeFrom=, jobTimeTo= 처럼 빈 값이 온다
    mockMvc.perform(get("/api/sync-jobs").param("jobTimeFrom", "").param("jobTimeTo", ""))
        .andExpect(status().isOk());

    ArgumentCaptor<SyncJobSearchCondition> captor = ArgumentCaptor.forClass(SyncJobSearchCondition.class);
    verify(syncJobQueryService).getSyncJobs(captor.capture());
    assertThat(captor.getValue().jobTimeFrom()).isNull();
    assertThat(captor.getValue().jobTimeTo()).isNull();
  }
}