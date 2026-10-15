package com.sprint.mission.sb15findexteam1.dashboard.calculator;


import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexPerformanceDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FluctuationCalculatorTest {

  // 테스트할 계산 클래스 (아직 없어서 빨간 줄이 정상)
  private final FluctuationCalculator calculator = new FluctuationCalculator();

  // 기준가 100 → 현재가 105: 5 오르고 5.00% 오른 것
  @Test
  @DisplayName("현재가가 기준가보다 크면 양수 등락과 등락률이 나온다")
  void rise() {
    // 지수 정보는 계산과 무관하므로 임의 값(1L, "KOSPI시리즈", "코스피")을 넣는다
    IndexPerformanceDto result = calculator.calculate(1L, "KOSPI시리즈", "코스피",
                                                        new BigDecimal("105"),new BigDecimal("100"));
    // 지수 정보는 입력 그대로 전달되어야 한다
    assertThat(result.indexInfoId()).isEqualTo(1L);
    assertThat(result.indexName()).isEqualTo("코스피");

    // 현재가와 기준가도 그대로 담긴다
    assertThat(result.currentPrice()).isEqualByComparingTo("105");
    assertThat(result.beforePrice()).isEqualByComparingTo("100");

    // 계산 결과: 등락 = 105 - 100 = 5, 등락률 = 5 ÷ 100 × 100 = 5.00
    assertThat(result.versus()).isEqualByComparingTo("5.00");
    assertThat(result.fluctuationRate()).isEqualByComparingTo("5.00");
  }

  // 기준가 100 → 현재가 95: 5 내리고 -5.00% 내린 것
  @Test
  @DisplayName("현재가가 기준가보다 작으면 음수 등락과 등락률이 나온다")
  void fall() {
    IndexPerformanceDto result = calculator.calculate(1L, "KOSPI시리즈", "코스피",
                                                        new BigDecimal("95"),new BigDecimal("100"));

    // 등락 = 95 - 100 = -5
    assertThat(result.versus()).isEqualByComparingTo("-5.00");
    // 등락률 = -5 ÷ 100 × 100 = -5.00
    assertThat(result.fluctuationRate()).isEqualByComparingTo("-5.00");
  }

  // 나누어떨어지지 않으면 소수 둘째 자리에서 반올림한다
  @Test
  @DisplayName("나누어떨어지지 않는 등락률은 소수 둘째 자리에서 반올림한다")
  void rounding() {
    IndexPerformanceDto result = calculator.calculate(
        1L, "KOSPI시리즈", "코스피",new BigDecimal("10"),new BigDecimal("3"));

    // 등락 = 10 - 3 = 7
    assertThat(result.versus()).isEqualByComparingTo("7");
    // 등락률 = 7 ÷ 3 × 100 = 233.3333... → 소수 둘째 자리 반올림 → 233.33
    assertThat(result.fluctuationRate()).isEqualByComparingTo("233.33");
  }

}