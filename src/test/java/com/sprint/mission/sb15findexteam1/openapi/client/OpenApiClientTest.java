package com.sprint.mission.sb15findexteam1.openapi.client;

import java.time.LocalDate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("실제 API 키와 네트워크가 필요한 수동 연동 확인용")
@SpringBootTest
class OpenApiClientTest {

  @Autowired
  OpenApiClient openApiClient;

  @Test
  void 실제_요청_테스트() {
    var items = openApiClient.getIndexData(LocalDate.of(2026,10,10), LocalDate.of(2026,10,14));

    System.out.println(items.size());
    System.out.println("Response: " + items);

  }
}
