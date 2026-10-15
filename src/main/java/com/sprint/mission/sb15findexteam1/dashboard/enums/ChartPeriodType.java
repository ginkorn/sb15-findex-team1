package com.sprint.mission.sb15findexteam1.dashboard.enums;

import java.time.LocalDate;

public enum ChartPeriodType {
  MONTHLY, QUARTERLY, YEARLY;

  // 기준일(to)로부터 시작 날짜를 계산한다
  public LocalDate startDate(LocalDate to) {
    // this = 지금 호출된 enum 값 (MONTHLY, QUARTERLY, YEARLY 중 하나)
    return switch (this) {
      case MONTHLY   -> to.minusMonths(1);    // 1개월 전
      case QUARTERLY -> to.minusMonths(3);    // 3개월 전
      case YEARLY    -> to.minusYears(1);     // 1년 전
    };
  }
}
