package com.sprint.mission.sb15findexteam1.common.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙을 어겼을 때 서비스에서 직접 던지는 예외.
 *
 * <p>"비즈니스 규칙"이란 노션 요구사항에 적힌 규칙들이다.
 * 예: 같은 지수는 두 번 등록할 수 없다, 없는 지수는 조회할 수 없다.
 *
 * <p><b>사용법</b>
 * <pre>
 * // 상세 정보 없이
 * throw new BusinessException(ErrorCode.INDEX_INFO_NOT_FOUND);
 *
 * // 상세 정보와 함께 (응답의 details로 나간다. 원인 찾기가 쉬워진다)
 * throw new BusinessException(ErrorCode.INDEX_INFO_NOT_FOUND, "id=" + id);
 * </pre>
 *
 * <p>던지기만 하면 GlobalExceptionHandler가 받아서 ErrorCode의 상태 코드·메시지로 응답한다.
 * 컨트롤러에서 try-catch를 쓸 필요가 없다.
 *
 * <p>왜 RuntimeException을 상속하나?
 * RuntimeException(언체크 예외)은 메서드에 {@code throws}를 적지 않아도 던질 수 있고,
 * {@code @Transactional} 메서드 안에서 던지면 DB 작업이 자동으로 취소(롤백)된다.
 */
@Getter
public class BusinessException extends RuntimeException {

  /** 어떤 종류의 에러인지 (상태 코드와 기본 메시지를 가지고 있다) */
  private final ErrorCode errorCode;

  /** 원인 파악용 상세 정보. 응답의 details로 나간다. 없으면 null */
  private final String details;

  public BusinessException(ErrorCode errorCode) {
    this(errorCode, null);
  }

  public BusinessException(ErrorCode errorCode, String details) {
    // 서버 로그에 찍히는 예외 메시지: "기본 메시지 + 상세 정보"
    super(details == null ? errorCode.getMessage() : errorCode.getMessage() + " " + details);
    this.errorCode = errorCode;
    this.details = details;
  }
}
