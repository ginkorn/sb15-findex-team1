package com.sprint.mission.sb15findexteam1.indexdata.dto;

import java.math.BigDecimal;

public record IndexDataUpdateRequest(
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount
) {
    // PATCH: null 값은 서비스 계층에서 '변경하지 않음'으로 처리할 예정
}
