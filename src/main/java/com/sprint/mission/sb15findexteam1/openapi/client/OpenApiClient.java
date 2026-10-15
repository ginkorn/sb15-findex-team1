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
