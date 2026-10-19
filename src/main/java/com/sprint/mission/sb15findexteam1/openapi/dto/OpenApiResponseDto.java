package com.sprint.mission.sb15findexteam1.openapi.dto;

import java.util.Collections;
import java.util.List;

/**
 * 금융위원회 Open API 응답 JSON에 대응하는 DTO.
 *
 * <p>구조: response → header(결과 코드) / body → items → item(시세 한 줄)
 * <p>Open API는 값을 모두 문자열로 보내서 Item도 String으로 받고, 저장할 때 변환한다.
 */
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
      String basPntm,     // 지수를 산출하기 위한 기준시점. 일부 지수는 null이므로 저장 전 검증
      String epyItmsCnt, // 채용 종목 수 -> 지수가 채용한 종목의 수 / ex. 25 / Integer
      String basDt,    // 기준일자 / LocalDate
      String basIdx,  // 기준 지수. 일부 지수는 null이므로 저장 전 검증
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

  /** 시세 목록만 꺼낸다. 없으면 빈 리스트. */
  public List<Item> extractItems() {
    if (response == null || response.body == null
        || response.body.items == null || response.body.items.item == null) {
      return Collections.emptyList();
    }

    return response.body.items.item;
  }

  /** resultCode가 00이면 정상 응답. */
  public boolean isSuccess() {
    return response != null && response.header != null
        && "00".equals(response.header.resultCode);
  }

  /** 예외 메시지용 "결과코드 결과메시지". */
  public String errorMessage() {
    if (response == null || response.header == null) return "응답 없음";
    return response.header.resultCode + " " + response.header.resultMsg;
  }


}
