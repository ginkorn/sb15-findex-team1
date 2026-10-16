package com.sprint.mission.sb15findexteam1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 팀 공통 에러 목록. 에러마다 HTTP 상태 코드와 기본 메시지를 가진다.
 *
 * <p>사용법: 필요한 에러를 아래에 추가하고, 서비스에서
 * {@code throw new BusinessException(ErrorCode.XXX, "상세 정보")}로 던진다.
 * 응답 JSON은 GlobalExceptionHandler가 자동으로 만든다.
 *
 * <p>상태 코드 기준 (Swagger 명세 기준)
 * <ul>
 *   <li>400: 요청 값이 잘못됨, 중복 등록</li>
 *   <li>404: 대상이 없음</li>
 *   <li>500: 서버 내부 오류, 외부 API 오류</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // ---- 공통 ----
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

  // ---- 지수 정보 (A) ----
  INDEX_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "지수 정보를 찾을 수 없습니다."),
  DUPLICATE_INDEX_INFO(HttpStatus.BAD_REQUEST, "이미 존재하는 지수입니다."),

  // ---- 자동 연동 설정 (A) ----
  AUTO_SYNC_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "자동 연동 설정을 찾을 수 없습니다."),

  // ---- 지수 데이터 (B) ----
  INDEX_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "지수 데이터를 찾을 수 없습니다."),
  DUPLICATE_INDEX_DATA(HttpStatus.BAD_REQUEST, "이미 등록된 지수 데이터입니다.");

  // C, D도 필요한 에러를 이 아래에 추가하면 된다.

  private final HttpStatus status;
  private final String message;
}
