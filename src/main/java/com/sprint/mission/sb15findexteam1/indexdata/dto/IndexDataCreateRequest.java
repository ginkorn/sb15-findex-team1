package com.sprint.mission.sb15findexteam1.indexdata.dto;

import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto.Item;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record IndexDataCreateRequest(
    Long indexInfoId,
    LocalDate baseDate,
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

  private static final DateTimeFormatter KRX_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  /**
   * Open API 항목의 지수 데이터 값을 변환한다.
   * 대상 IndexInfo는 별도로 조회해야 하므로 indexInfoId는 아직 설정하지 않는다.
   */
  public static IndexDataCreateRequest fromOpenApi(Item item) {
    return new IndexDataCreateRequest(
        null,
        LocalDate.parse(item.basDt(), KRX_DATE_FORMAT),
        new BigDecimal(item.mkp()),
        new BigDecimal(item.clpr()),
        new BigDecimal(item.hipr()),
        new BigDecimal(item.lopr()),
        new BigDecimal(item.vs()),
        new BigDecimal(item.fltRt()),
        Long.valueOf(item.trqu()),
        Long.valueOf(item.trPrc()),
        Long.valueOf(item.lstgMrktTotAmt())
    );
  }
}
