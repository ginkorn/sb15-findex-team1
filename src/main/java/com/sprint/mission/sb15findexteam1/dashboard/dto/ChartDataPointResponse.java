package com.sprint.mission.sb15findexteam1.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataPointResponse (
  @Schema(description = "날짜",example = "2023-01-01")
  LocalDate date,

  @Schema(description = "값", example = "2850.75")
  BigDecimal value
){
}
