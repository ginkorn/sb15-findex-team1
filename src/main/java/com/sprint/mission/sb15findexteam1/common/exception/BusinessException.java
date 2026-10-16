package com.sprint.mission.sb15findexteam1.common.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙을 어겼을 때 서비스에서 직접 던지는 예외.
 *
 * <pre>
 * throw new BusinessException(ErrorCode.INDEX_INFO_NOT_FOUND, "id=" + id);
 * </pre>
 *
 * <p>GlobalExceptionHandler가 받아서 ErrorCode의 상태 코드와 메시지로 응답한다.
 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  /** 원인 파악용 상세 정보. 응답의 details로 나간다. */
  private final String details;

  public BusinessException(ErrorCode errorCode) {
    this(errorCode, null);
  }

  public BusinessException(ErrorCode errorCode, String details) {
    super(details == null ? errorCode.getMessage() : errorCode.getMessage() + " " + details);
    this.errorCode = errorCode;
    this.details = details;
  }
}
