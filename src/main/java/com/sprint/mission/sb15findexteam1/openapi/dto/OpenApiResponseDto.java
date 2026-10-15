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
      List<OpenApiResponseDto> item
  ) {}


  // 편의성
  public List<OpenApiResponseDto> extractItems() {
    if (response == null || response.body == null
        || response.body.items == null || response.body.items.item == null) {
      return Collections.emptyList();
    }

    return response.body.items.item;
  }


}
