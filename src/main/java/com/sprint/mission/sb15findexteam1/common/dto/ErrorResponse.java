package com.sprint.mission.sb15findexteam1.common.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatusCode;

/**
 * 모든 에러 응답의 공통 모양. (Swagger 명세의 ErrorResponse와 필드 이름까지 같다)
 *
 * <p>프론트엔드는 에러가 나면 이 JSON의 message를 화면(토스트)에 띄운다.
 * 그래서 누가 만든 API에서 에러가 나든 항상 같은 모양이어야 한다.
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
 * <p><b>직접 만들 일은 거의 없다.</b> 서비스에서 {@code BusinessException}을 던지면
 * {@code GlobalExceptionHandler}가 자동으로 이 모양으로 바꿔서 응답한다.
 *
 * <p>왜 record인가? record는 "값을 담기만 하는 클래스"를 짧게 쓰는 문법이다.
 * 생성자, getter(timestamp() 처럼 필드 이름 그대로), equals/hashCode/toString을 자동으로 만들어 주고,
 * 한 번 만들면 값을 바꿀 수 없어서 응답 DTO로 쓰기 좋다.
 *
 * @param timestamp 에러가 발생한 시각
 * @param status    HTTP 상태 코드 숫자 (400, 404, 500 ...)
 * @param message   사용자에게 보여 줄 에러 설명 (ErrorCode의 기본 메시지)
 * @param details   개발자가 원인을 찾기 위한 상세 정보 (어떤 id, 어떤 필드 등). 없으면 null
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    String details
) {

  /**
   * 현재 시각으로 에러 응답을 만든다.
   *
   * @param status  HttpStatus.NOT_FOUND 처럼 상태 코드 객체 (숫자로 바꿔서 담는다)
   * @param message 사용자용 메시지
   * @param details 상세 정보 (없으면 null)
   */
  public static ErrorResponse of(HttpStatusCode status, String message, String details) {
    return new ErrorResponse(LocalDateTime.now(), status.value(), message, details);
  }
}
