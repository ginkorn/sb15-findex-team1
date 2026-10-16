package com.sprint.mission.sb15findexteam1.common.exception;

import com.sprint.mission.sb15findexteam1.common.dto.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 컨트롤러에서 발생한 예외를 한곳에서 받아 ErrorResponse JSON으로 바꾸는 처리기.
 *
 * <p>고객센터 응대 매뉴얼처럼, 누가 만든 코드에서 에러가 나든 프론트엔드는 항상
 * {@code { timestamp, status, message, details }} 같은 모양을 받는다.
 *
 * <p>처리 순서: 구체적인 예외가 먼저 잡히고, 나머지는 마지막 Exception 처리기가 500으로 응답한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 1) 우리가 직접 던진 예외 → ErrorCode에 적힌 상태 코드로 응답 */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    return ResponseEntity.status(code.getStatus())
        .body(ErrorResponse.of(code.getStatus(), code.getMessage(), e.getDetails()));
  }

  /** 2) @Valid 검사 실패 (필수 값 누락, 범위 초과 등) → 400, details에 틀린 필드 목록 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    String details = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return badRequest(details);
  }

  /**
   * 3) 요청 형식이 틀림 → 400
   * <ul>
   *   <li>HandlerMethodValidationException: @RequestParam 등에 붙인 검증 실패</li>
   *   <li>MethodArgumentTypeMismatchException: 숫자 자리에 문자, 잘못된 날짜·enum 값</li>
   *   <li>HttpMessageNotReadableException: JSON 문법 오류, 날짜 형식 오류</li>
   *   <li>MissingServletRequestParameterException: 필수 쿼리 파라미터 누락</li>
   *   <li>IllegalArgumentException: 기존 코드에서 쓰던 검증 예외 (BusinessException으로 점차 교체)</li>
   * </ul>
   */
  @ExceptionHandler({
      HandlerMethodValidationException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class,
      MissingServletRequestParameterException.class,
      IllegalArgumentException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
    return badRequest(e.getMessage());
  }

  /** 4) DB 제약 위반 (동시에 같은 값을 등록해 유니크 제약에 걸린 경우 등) → 400 */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraint(DataIntegrityViolationException e) {
    return badRequest("데이터 제약 조건을 위반했습니다.");
  }

  /** 5) 없는 주소 → 404 (이 처리기가 없으면 아래 6번이 500으로 바꿔 버린다) */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, ErrorCode.RESOURCE_NOT_FOUND.getMessage(), e.getResourcePath()));
  }

  /** 지원하지 않는 HTTP 메서드 (예: GET만 있는 주소에 DELETE) → 405 */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, "지원하지 않는 요청 방식입니다.", e.getMessage()));
  }

  /** 6) 그 밖의 모든 예외 → 500. 원인 파악을 위해 서버 로그에 전체 스택을 남긴다. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
    log.error("처리되지 않은 예외", e);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, ErrorCode.INTERNAL_ERROR.getMessage(), e.getMessage()));
  }

  private ResponseEntity<ErrorResponse> badRequest(String details) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, ErrorCode.INVALID_INPUT.getMessage(), details));
  }
}
