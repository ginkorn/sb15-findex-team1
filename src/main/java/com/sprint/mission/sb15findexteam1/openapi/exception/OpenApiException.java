package com.sprint.mission.sb15findexteam1.openapi.exception;

/** Open API 호출 실패(응답 없음, resultCode가 00이 아님)를 나타내는 예외. */
public class OpenApiException extends RuntimeException {
  public OpenApiException(String message) {
    super(message);
  }
}
