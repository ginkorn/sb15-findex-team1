package com.sprint.mission.sb15findexteam1.dashboard.controller;

import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;   // 차트 계산은 서비스가 담당

  @Operation(summary = "지수 차트 조회")
  @GetMapping("/{id}/chart")
  public IndexChartDto getChart(@PathVariable Long id,@RequestParam(defaultValue = "MONTHLY") ChartPeriodType periodType) {
    return dashboardService.getChart(id, periodType);   // 서비스 결과를 그대로 응답
  }

}
