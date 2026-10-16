package com.sprint.mission.sb15findexteam1.common.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatusCode;

/**
 * 모든 에러 응답의 공통 모양. (Swagger 명세의 ErrorResponse와 같다)
 *
 * <pre>
 * {
 *   "timestamp": "2026-10-16T10:00:00",
 *   "status": 404,
 *   "message": "지수 정보를 찾을 수 없습니다.",
 *   "details": "id=99"
 * }
 * </pre>
 *
 * <p>직접 만들 일은 거의 없다. 서비스에서 BusinessException을 던지면
 * GlobalExceptionHandler가 이 모양으로 바꿔 응답한다.
 *
 * @param timestamp 에러가 발생한 시각
 * @param status    HTTP 상태 코드 숫자 (400, 404, 500 ...)
 * @param message   사용자에게 보여줄 에러 설명
 * @param details   원인 파악용 상세 정보 (어떤 id, 어떤 필드 등). 없으면 null
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    String details
) {

  public static ErrorResponse of(HttpStatusCode status, String message, String details) {
    return new ErrorResponse(LocalDateTime.now(), status.value(), message, details);
  }
}
