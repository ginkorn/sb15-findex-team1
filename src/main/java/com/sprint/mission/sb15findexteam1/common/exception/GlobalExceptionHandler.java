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
 * 모든 컨트롤러의 예외를 한곳에서 받아 ErrorResponse JSON으로 바꿔 주는 "에러 응대 창구".
 *
 * <p>고객센터 응대 매뉴얼처럼, 누가 만든 코드에서 에러가 나든 프론트엔드는 항상
 * {@code { timestamp, status, message, details }} 같은 모양을 받는다.
 *
 * <p><b>동작 흐름</b>
 * <pre>
 * 서비스에서 예외 발생 → 컨트롤러 밖으로 던져짐 → 여기서 예외 종류에 맞는 메서드가 받음
 *   → ErrorResponse로 바꿔서 알맞은 상태 코드로 응답
 * </pre>
 *
 * <ul>
 *   <li>{@code @RestControllerAdvice}: 모든 @RestController에 공통으로 적용되는 예외 처리기라는 표시</li>
 *   <li>{@code @ExceptionHandler(X.class)}: "X 예외가 나면 이 메서드가 처리한다"는 표시.
 *       여러 처리기가 해당되면 더 구체적인 예외를 받는 메서드가 먼저 선택된다.</li>
 *   <li>{@code @Slf4j}: log 객체를 Lombok이 만들어 준다. (서버 로그 기록용)</li>
 * </ul>
 *
 * <p>팀원이 할 일: 예외를 던지기만 하면 된다. 이 클래스는 거의 고칠 일이 없다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 1) 우리가 직접 던진 BusinessException → ErrorCode에 적힌 상태 코드로 응답.
   * 예: 중복 등록 → 400, 없는 지수 → 404
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    return ResponseEntity.status(code.getStatus())
        .body(ErrorResponse.of(code.getStatus(), code.getMessage(), e.getDetails()));
  }

  /**
   * 2) @RequestBody에 붙인 @Valid 검사 실패 → 400.
   * details에 어떤 필드가 왜 틀렸는지 모아서 보낸다.
   * 예: "baseIndex: 널이어서는 안됩니다, indexName: 공백일 수 없습니다"
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    String details = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return badRequest(details);
  }

  /**
   * 3) 요청 형식이 틀림 → 400. 아래 예외들을 한꺼번에 받는다.
   * <ul>
   *   <li>HandlerMethodValidationException: @RequestParam, @PathVariable 등에 붙인 검증 실패</li>
   *   <li>MethodArgumentTypeMismatchException: 숫자 자리에 문자를 보냄, 없는 enum 값을 보냄
   *       (예: /api/index-infos/abc)</li>
   *   <li>HttpMessageNotReadableException: JSON 문법 오류, 날짜 형식 오류 (예: "2026/10/16")</li>
   *   <li>MissingServletRequestParameterException: 필수 쿼리 파라미터를 빠뜨림</li>
   *   <li>IllegalArgumentException: 기존 코드(B 등)에서 쓰던 검증 예외.
   *       점차 BusinessException으로 바꾸면 404 등 더 정확한 코드로 응답할 수 있다.</li>
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

  /**
   * 4) DB 제약 조건 위반 → 400.
   * 서비스에서 중복 검사를 했더라도, 같은 요청이 거의 동시에 두 번 들어오면
   * 둘 다 검사를 통과하고 DB의 유니크 제약에서 막힌다. 그때 500이 아니라 400으로 응답한다.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraint(DataIntegrityViolationException e) {
    return badRequest("데이터 제약 조건을 위반했습니다.");
  }

  /**
   * 5) 없는 주소로 요청 → 404.
   * 이 처리기가 없으면 아래 6번(Exception 전체)이 받아서 500으로 바꿔 버리기 때문에 꼭 필요하다.
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, ErrorCode.RESOURCE_NOT_FOUND.getMessage(), e.getResourcePath()));
  }

  /** 지원하지 않는 HTTP 메서드 → 405. 예: GET만 있는 주소에 DELETE를 보냄 */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, "지원하지 않는 요청 방식입니다.", e.getMessage()));
  }

  /**
   * 6) 위에서 처리하지 못한 모든 예외 → 500.
   * 예상하지 못한 에러이므로 서버 로그에 전체 내용(스택 트레이스)을 남긴다.
   * 500이 보이면 IntelliJ 콘솔에서 "처리되지 않은 예외" 로그를 찾아보면 원인이 나온다.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
    log.error("처리되지 않은 예외", e);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, ErrorCode.INTERNAL_ERROR.getMessage(), e.getMessage()));
  }

  /** 400 응답을 만드는 공통 도우미 */
  private ResponseEntity<ErrorResponse> badRequest(String details) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, ErrorCode.INVALID_INPUT.getMessage(), details));
  }
}
