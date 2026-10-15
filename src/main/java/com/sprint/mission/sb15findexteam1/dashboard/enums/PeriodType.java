package com.sprint.mission.sb15findexteam1.dashboard.enums;

import java.time.LocalDate;

public enum PeriodType {
  DAILY,WEEKLY,MONTHLY;

  public LocalDate baseDate(LocalDate current) {
    return switch (this) {
      case DAILY -> current.minusDays(1);        // 일간: 하루 전
      case WEEKLY -> current.minusWeeks(1);      // 주간: 7일 전
      case MONTHLY -> current.minusMonths(1);    // 월간: 한 달 전
    };
  }
}


