package com.sprint.mission.sb15findexteam1.dashboard.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.sb15findexteam1.dashboard.dto.ChartDataPointResponse;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexPerformanceDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.enums.PeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.service.DashboardService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// 컨트롤러(웹 계층)만 띄우는 테스트. 서비스·DB는 띄우지 않는다.
@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

  @Autowired
  MockMvc mockMvc;               // 가짜 HTTP 요청을 보내는 도구 (서버를 실제로 켜지 않고 호출)

  @MockitoBean
  DashboardService dashboardService;   // 진짜 서비스 대신 가짜 서비스를 스프링에 등록

  @Test
  @DisplayName("차트 조회: 서비스 결과를 JSON으로 응답한다")
  void getChart() throws Exception {
    // given: 서비스가 이 결과를 돌려준다고 가정
    IndexChartDto dto = new IndexChartDto(
        1L, "KOSPI시리즈", "IT 서비스", "MONTHLY",
        List.of(new ChartDataPointResponse(LocalDate.of(2025, 1, 14), new BigDecimal("100"))),
        List.of(), List.of());
    given(dashboardService.getChart(1L, ChartPeriodType.MONTHLY)).willReturn(dto);

    // when & then: GET /api/index-data/1/chart?periodType=MONTHLY 호출
    mockMvc.perform(get("/api/index-data/1/chart").param("periodType", "MONTHLY"))
        .andExpect(status().isOk())                                   // 200
        .andExpect(jsonPath("$.indexInfoId").value(1))                 // 응답 JSON 필드 검증
        .andExpect(jsonPath("$.periodType").value("MONTHLY"))
        .andExpect(jsonPath("$.dataPoints[0].value").value(100));
  }

  @Test
  @DisplayName("관심 지수 성과: 즐겨찾기 지수의 성과를 JSON 목록으로 응답한다")
  void getFavoritePerformances() throws Exception {
    // given: 서비스가 지수 1개의 성과를 돌려준다고 가정
    IndexPerformanceDto dto = new IndexPerformanceDto(
        1L, "KOSPI시리즈", "코스피",
        new BigDecimal("10"), new BigDecimal("10.00"),
        new BigDecimal("110"), new BigDecimal("100"));
    given(dashboardService.getFavoritePerformances(PeriodType.DAILY)).willReturn(List.of(dto));

    // when & then: GET /api/index-data/performance/favorite?periodType=DAILY
    mockMvc.perform(get("/api/index-data/performance/favorite").param("periodType", "DAILY"))
        .andExpect(status().isOk())                                   // 200
        .andExpect(jsonPath("$[0].indexInfoId").value(1))              // 목록의 첫 번째 항목
        .andExpect(jsonPath("$[0].indexName").value("코스피"))
        .andExpect(jsonPath("$[0].fluctuationRate").value(10.00))
        .andExpect(jsonPath("$[0].currentPrice").value(110))
        .andExpect(jsonPath("$[0].beforePrice").value(100));
  }

  @Test
  @DisplayName("관심 지수 성과: periodType을 생략하면 DAILY로 조회하고, 즐겨찾기가 없으면 빈 배열을 응답한다")
  void getFavoritePerformances_defaultAndEmpty() throws Exception {
    // given: 즐겨찾기가 없어서 서비스가 빈 목록을 돌려준다
    given(dashboardService.getFavoritePerformances(PeriodType.DAILY)).willReturn(List.of());

    // when & then: periodType 파라미터 없이 호출 → DAILY가 기본값이어야 한다
    mockMvc.perform(get("/api/index-data/performance/favorite"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());                          // 빈 배열 []
  }
}