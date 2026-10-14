package com.sprint.mission.sb15findexteam1.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class IndexChartDto {

  @Schema(description = "지수 정보", example = "1")
  private Integer indexInfoId;

  @Schema(description = "지수 분류명", example = "KOSPI시리즈")
  private String indexClassification;

  @Schema(description = "지수명", example = "IT 서비스")
  private String indexName;

  @Schema(description = "차트 기간 유형 (DAILY, WEEKLY, MONTHLY)", example = "DAILY")
  private String periodType;

  @Schema(description = "차트 데이터 목록")
  private List<ChartDataPointResponse> dataPoints;

  @Schema(description = "5일 이동평균선 데이터 목록")
  private List<ChartDataPointResponse> ma5DataPoints;

  @Schema(description = "20일 이동평균선 데이터 목록")
  private List<ChartDataPointResponse> ma20DataPoints;

}
