package com.sprint.mission.sb15findexteam1.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record RankedIndexPerformanceDto(
    @Schema(description = "IndexPerformanceDto")
    IndexPerformanceDto performance,

    @Schema(description = "순위",example = "1")
    BigDecimal rank
) {
}
