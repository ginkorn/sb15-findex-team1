package com.sprint.mission.sb15findexteam1.dashboard.calculator;

import com.sprint.mission.sb15findexteam1.dashboard.dto.ChartDataPointResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MovingAverageCalculator {

  // 이동평균을 계산하는 메서드
  // prices : 날짜 오름차순으로 정렬된 종가 목록 (날짜 + 종가)
  // window : 몇 개씩 묶어서 평균을 낼지 (5일선이면 5, 20일선이면 20)
  public List<ChartDataPointResponse> calculate(List<ChartDataPointResponse> prices, int window) {

    // window가 1보다 작으면 평균을 낼 수 없으므로 즉시 예외 (테스트가 기대하는 동작)
    if (window < 1) {
      throw new IllegalArgumentException("window는 1 이상이어야 합니다.");
    }
    // 계산 결과(날짜 + 평균)를 담을 빈 리스트를 만든다
    List<ChartDataPointResponse> result = new ArrayList<>();

    // i는 "평균을 구할 날짜"의 위치(인덱스)이다.
    // window개가 모이는 첫 날(window - 1번 위치)부터 마지막 날까지 하나씩 이동한다.
    // 예) window=5이면 i는 4부터 시작 (0~4번, 5개가 모인 날)
    for (int i = window - 1; i < prices.size(); i++) {

      // 합계를 0에서 시작한다. BigDecimal은 + 대신 add()를 쓴다.
      BigDecimal sum = BigDecimal.ZERO;

      // i번째 날을 끝으로 하는 최근 window개를 더한다.
      // 예) window=5, i=4이면 j는 0,1,2,3,4
      for (int j = i - window + 1; j <= i; j++) {
        sum = sum.add(prices.get(j).value());   // j번째 종가를 합계에 더함
      }

      // 합계를 window로 나눠 평균을 구한다.
      // 2 : 소수점 둘째 자리까지 남긴다
      // RoundingMode.HALF_UP : 반올림 (예: 1.335 -> 1.34)
      BigDecimal average = sum.divide(BigDecimal.valueOf(window), 2, RoundingMode.HALF_UP);

      // (i번째 날짜, 평균값)을 결과 목록에 추가한다
      result.add(new ChartDataPointResponse(prices.get(i).date(), average));
    }

    // 모든 날짜의 계산이 끝난 목록을 돌려준다
    return result;
  }
}
