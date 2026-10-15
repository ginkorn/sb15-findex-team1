package com.sprint.mission.sb15findexteam1.openapi.client;


import com.sprint.mission.sb15findexteam1.openapi.exception.OpenApiException;
import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto;
import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto.Item;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 금융위원회 지수시세 Open API를 호출하는 클라이언트.
 *
 * <p>주소·인증키는 .env의 ENDPOINT, API_KEY에서 읽는다. (없으면 기본 주소, 빈 키)
 */
@Component
@Slf4j
public class OpenApiClient {
  private static final DateTimeFormatter KRX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final RestClient restClient;
  private String apiKey;

  public OpenApiClient(
      @Value("${ENDPOINT:https://apis.data.go.kr/1160100/GetMarketIndexInfoService_V2/getStockMarketIndex_V2}") String url,
      @Value("${API_KEY:}") String apiKey) {
    this.restClient = RestClient.builder().baseUrl(url).build();
    this.apiKey = apiKey;
  }

  /**
   * 기간 안의 모든 지수 시세를 가져온다. (페이지를 끝까지 반복 조회)
   *
   * <p>Open API의 endBasDt는 "미만" 조건이라 종료일에 하루를 더해 보낸다.
   *
   * @throws OpenApiException 응답이 없거나 resultCode가 00(정상)이 아닐 때
   */
  public List<Item> getIndexData(LocalDate baseDateFrom, LocalDate baseDateTo) {
    String beginDt = baseDateFrom.format(KRX_DATE_FORMAT);
    String endDt = baseDateTo.plusDays(1).format(KRX_DATE_FORMAT);
    List<Item> items = new ArrayList<>();
    OpenApiResponseDto response;
    int pageNo = 1;

    while (true) {
      int currentPage = pageNo;
      response = restClient.get()
          .uri(uriBuilder -> uriBuilder
              .queryParam("serviceKey", apiKey)
              .queryParam("resultType", "json")
              .queryParam("numOfRows", 1000) // 안 쓰면 기본값이 10이라서 10개의 데이터만 옴
              .queryParam("pageNo", currentPage)
              .queryParam("beginBasDt", beginDt)
              .queryParam("endBasDt", endDt)
              .build())
          .retrieve()
          .body(OpenApiResponseDto.class);

      if (response == null) throw new OpenApiException("응답 없음");
      if (!response.isSuccess()) throw new OpenApiException(response.errorMessage());

      List<Item> page = response.extractItems();
      items.addAll(page);

      // while 문 종료 조건 -> 전부 받았거나 더 받을 게 없으면 종료
      if (items.size() >= response.response().body().totalCount() || page.isEmpty()) {
        break;
      }

      pageNo++;
    }


    return items;
  }
}
