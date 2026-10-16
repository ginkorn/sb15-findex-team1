package com.sprint.mission.sb15findexteam1.dashboard.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 성과 기간 유형(DAILY/WEEKLY/MONTHLY)별로 "비교할 기준일"이 맞게 계산되는지 확인하는 테스트
class PeriodTypeTest {

  @Test
  @DisplayName("DAILY는 하루 전을 기준일로 한다")
  void daily() {
    LocalDate result = PeriodType.DAILY.baseDate(LocalDate.of(2025, 1, 15));

    assertThat(result).isEqualTo(LocalDate.of(2025, 1, 14));
  }

  @Test
  @DisplayName("WEEKLY는 일주일 전을 기준일로 한다")
  void weekly() {
    LocalDate result = PeriodType.WEEKLY.baseDate(LocalDate.of(2025, 1, 15));

    assertThat(result).isEqualTo(LocalDate.of(2025, 1, 8));
  }

  @Test
  @DisplayName("MONTHLY는 한 달 전을 기준일로 한다")
  void monthly() {
    LocalDate result = PeriodType.MONTHLY.baseDate(LocalDate.of(2025, 2, 15));

    assertThat(result).isEqualTo(LocalDate.of(2025, 1, 15));
  }
}