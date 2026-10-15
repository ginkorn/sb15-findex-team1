package com.sprint.mission.sb15findexteam1.dashboard.calculator;

import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexPerformanceDto;
import java.math.BigDecimal;
import java.math.RoundingMode;                 // 반올림 방식을 쓰기 위해 추가
import org.springframework.stereotype.Component;

@Component
public class FluctuationCalculator {

  // 지수 정보와 현재가·기준가를 받아서, 등락/등락률이 계산된 응답 DTO를 돌려준다
  public IndexPerformanceDto calculate(Long indexInfoId,String indexClassification,String indexName,BigDecimal currentPrice,BigDecimal beforePrice) {
    // 등락 = 현재가 - 기준가  (예: 105 - 100 = 5)
    BigDecimal versus = currentPrice.subtract(beforePrice);

    // 등락률 = 등락 ÷ 기준가 × 100  (예: 5 ÷ 100 × 100 = 5.00)
    BigDecimal fluctuationRate;                      // 먼저 변수만 선언
    if (beforePrice.signum() == 0) {
      fluctuationRate = BigDecimal.ZERO;           // 기준가가 0이면 0
    } else {
      fluctuationRate = versus
          .multiply(BigDecimal.valueOf(100))
          .divide(beforePrice, 2, RoundingMode.HALF_UP);   // 아니면 기존 계산
    }
    // 계산한 값과 입력받은 값을 모두 담아 응답 DTO를 만든다
    return new IndexPerformanceDto(indexInfoId, indexClassification, indexName,versus, fluctuationRate, currentPrice, beforePrice);
  }
}