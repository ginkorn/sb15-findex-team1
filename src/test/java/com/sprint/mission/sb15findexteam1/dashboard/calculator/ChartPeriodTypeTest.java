package com.sprint.mission.sb15findexteam1.dashboard.calculator;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ChartPeriodTypeTest {

  // 기준일(마지막 데이터 날짜)을 2025-10-14로 가정한다
  private final LocalDate to = LocalDate.of(2025, 10, 14);

  // 1달 = 기준일에서 1개월 전
  @Test
  @DisplayName("MONTHLY는 기준일의 1개월 전부터 시작한다")
  void monthly() {
    assertThat(ChartPeriodType.MONTHLY.startDate(to))
        .isEqualTo(LocalDate.of(2025, 9, 14));
  }

  // 3달 = 기준일에서 3개월 전
  @Test
  @DisplayName("QUARTERLY는 기준일의 3개월 전부터 시작한다")
  void quarterly() {
    assertThat(ChartPeriodType.QUARTERLY.startDate(to))
        .isEqualTo(LocalDate.of(2025, 7, 14));
  }

  // 1년 = 기준일에서 1년 전
  @Test
  @DisplayName("YEARLY는 기준일의 1년 전부터 시작한다")
  void yearly() {
    assertThat(ChartPeriodType.YEARLY.startDate(to))
        .isEqualTo(LocalDate.of(2024, 10, 14));
  }

}
