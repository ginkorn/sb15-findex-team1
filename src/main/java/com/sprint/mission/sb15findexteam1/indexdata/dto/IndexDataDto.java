package com.sprint.mission.sb15findexteam1.indexdata.dto;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

// 지수 데이터 조회 결과를 프론트엔드에 전달하는 DTO
public record IndexDataDto(

    Long id,

    // 해당 데이터가 속한 지수 정보 ID
    Long indexInfoId,

    // 데이터의 기준 날짜
    LocalDate baseDate,

    // 데이터 출처: USER 또는 OPEN_API
    SourceType sourceType,

    // 시가
    BigDecimal marketPrice,

    // 종가
    BigDecimal closingPrice,

    // 고가
    BigDecimal highPrice,

    // 저가
    BigDecimal lowPrice,

    // 전일 대비
    BigDecimal versus,

    // 등락률
    BigDecimal fluctuationRate,

    // 거래량
    Long tradingQuantity,

    // 거래대금
    Long tradingPrice,

    // 시가총액
    Long marketTotalAmount

) {
}