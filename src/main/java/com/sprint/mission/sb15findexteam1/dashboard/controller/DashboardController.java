package com.sprint.mission.sb15findexteam1.dashboard.controller;

import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.periodType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
public class DashboardController {

  @Tag(name = "지수 차트 조회")
  @GetMapping("/{id}/chart")
  public IndexChartDto getChart(@PathVariable Long id,@RequestParam(defaultValue = "MONTHLY") periodType periodType) {
    return null;
  }
}
