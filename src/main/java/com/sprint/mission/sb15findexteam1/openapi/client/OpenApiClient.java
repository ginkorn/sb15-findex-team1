package com.sprint.mission.sb15findexteam1.openapi.client;


import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
      @Value("${ENDPOINT}") String url,
      @Value("${API_KEY}") String apiKey) {
    this.restClient = RestClient.builder().baseUrl(url).build();
    this.apiKey = apiKey;
  }

  public List<OpenApiResponseDto> getIndexData(LocalDate baseDateFrom, LocalDate baseDateTo) {
    String beginDt = baseDateFrom.format(KRX_DATE_FORMAT);
    String endDt = baseDateTo.format(KRX_DATE_FORMAT);

    OpenApiResponseDto response = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .queryParam("serviceKey", apiKey)
            .queryParam("resultType", "json")
            .queryParam("beginBasDt", beginDt)
            .queryParam("endBasDt", endDt)
            .build())
        .retrieve()
        .body(OpenApiResponseDto.class);

    return response.extractItems();
  }
}
