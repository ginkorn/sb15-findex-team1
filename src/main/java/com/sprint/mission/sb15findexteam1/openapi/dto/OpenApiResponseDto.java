package com.sprint.mission.sb15findexteam1.openapi.dto;

import java.util.Collections;
import java.util.List;

// 금융 API가 보내주는 Json은 4겹 구조로 이루어져있다.
// Response -> header, body -> items -> item
public record OpenApiResponseDto(
  Response response
) {

  public record Response(
      Header header,
      Body body
  ) {}

  public record Header(
      String resultCode,
      String resultMsg
  ) {}

  public record Body(
      int numOfRows,
      int pageNo,
      int totalCount,
      Items items
  ) {}

  public record Items(
      List<Item> item
  ) {}

  public record Item(
      String idxCsf,      // 지수분류명 / ex. KOSPI 시리즈
      String idxNm,       // 지수명 / ex. IT 서비스
      String basPntm,     // 지수를 산출하기 위한 기준시점 / ex. 20240701
      String epyItmsCnt, // 채용 종목 수 -> 지수가 채용한 종목의 수 / ex. 25 / Integer
      String basDt,    // 기준일자 / LocalDate
      String basIdx,  // 기준 지수 / BigDecimal
      String mkp,     // 하루 시가 / BigDecimal
      String clpr,    // 하루 종가 / BigDecimal
      String hipr,    // 하루 중 최고치 / BigDecimal
      String lopr,    // 하루 중 최저치 / BigDecimal
      String vs,      // 전일 대비 등락 / BigDecimal
      String fltRt,   // 전일 대비 등락 비율 / BigDecimal
      String trqu,          // 거래량 / Long
      String trPrc,         // 거래대금 / Long
      String lstgMrktTotAmt // 시가 총액 / Long
  ) {}

  // 편의성
  public List<Item> extractItems() {
    if (response == null || response.body == null
        || response.body.items == null || response.body.items.item == null) {
      return Collections.emptyList();
    }

    return response.body.items.item;
  }

  public boolean isSuccess() {
    return response != null && response.header != null
        && "00".equals(response.header.resultCode);
  }

  public String errorMessage() {
    if (response == null || response.header == null) return "응답 없음";
    return response.header.resultCode + " " + response.header.resultMsg;
  }


}
