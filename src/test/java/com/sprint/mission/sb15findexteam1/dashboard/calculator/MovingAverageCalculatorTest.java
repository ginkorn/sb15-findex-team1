package com.sprint.mission.sb15findexteam1.dashboard.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sprint.mission.sb15findexteam1.dashboard.dto.ChartDataPointResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MovingAverageCalculatorTest {

  private final MovingAverageCalculator calculator = new MovingAverageCalculator();

  private ChartDataPointResponse point(int day, String value) {
    return new ChartDataPointResponse(LocalDate.of(2025, 1, day), new BigDecimal(value));
  }

  @Test
  @DisplayName("데이터가 창 크기와 같으면 평균 1개가 나온다")
  void exactlyWindow() {
    List<ChartDataPointResponse> prices = List.of(
        point(2, "10"), point(3, "20"), point(4, "30"), point(5, "40"));

    List<ChartDataPointResponse> result = calculator.calculate(prices, 3);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2025, 1, 4));
    assertThat(result.get(0).value()).isEqualByComparingTo("20.00");
  }

  // 데이터가 window보다 적으면 결과는 비어 있어야 한다
  @Test
  @DisplayName("데이터가 창 크기보다 적으면 빈 리스트")
  void notEnoughData() {
    // 종가 2개뿐인데 5일 평균을 요청
    List<ChartDataPointResponse> prices = List.of(point(2, "100"), point(3, "102"));

    // 5개가 안 모이므로 평균을 만들 날이 없다
    assertThat(calculator.calculate(prices, 5)).isEmpty();
  }

  // 입력이 없으면 결과도 비어 있어야 한다
  @Test
  @DisplayName("입력이 비어 있으면 빈 리스트")
  void empty() {
    assertThat(calculator.calculate(List.of(), 5)).isEmpty();
  }

  // 나누어떨어지지 않으면 소수 둘째 자리에서 반올림한다
  @Test
  @DisplayName("나누어떨어지지 않으면 소수 둘째 자리에서 반올림")
  void rounding() {
    // (1 + 1 + 2) / 3 = 1.3333... → 반올림하면 1.33
    List<ChartDataPointResponse> prices = List.of(point(2, "1"), point(3, "1"), point(4, "2"));

    List<ChartDataPointResponse> result = calculator.calculate(prices, 3);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).value()).isEqualByComparingTo("1.33");
  }

  // window가 1보다 작으면 의미가 없으므로 예외를 던져야 한다
  @Test
  @DisplayName("window가 1보다 작으면 예외")
  void invalidWindow() {
    assertThatThrownBy(() -> calculator.calculate(List.of(), 0)).isInstanceOf(IllegalArgumentException.class);
  }
}