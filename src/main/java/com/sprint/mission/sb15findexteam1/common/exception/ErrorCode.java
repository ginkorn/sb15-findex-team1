package com.sprint.mission.sb15findexteam1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 팀 공통 에러 목록. 에러 하나마다 "HTTP 상태 코드 + 사용자에게 보여 줄 기본 메시지"를 정해 둔다.
 *
 * <p>왜 enum으로 모아 두나?
 * 메시지와 상태 코드를 코드 곳곳에 직접 쓰면 같은 에러인데 메시지가 다르거나,
 * 404여야 할 것이 500으로 나가는 실수가 생긴다. 한곳에 모아 두면 이름만 골라 쓰면 된다.
 *
 * <p><b>사용법</b>
 * <ol>
 *   <li>필요한 에러가 없으면 아래 목록에 한 줄 추가한다. (담당 구역 주석 아래에)</li>
 *   <li>서비스에서 {@code throw new BusinessException(ErrorCode.XXX, "상세 정보");}로 던진다.</li>
 *   <li>응답 JSON은 GlobalExceptionHandler가 자동으로 만든다.</li>
 * </ol>
 *
 * <p>상태 코드 기준 (Swagger 명세 기준)
 * <ul>
 *   <li>400 BAD_REQUEST: 요청 값이 잘못됨, 이미 있는 값을 또 등록함(중복)</li>
 *   <li>404 NOT_FOUND: 찾는 대상이 없음</li>
 *   <li>500 INTERNAL_SERVER_ERROR: 서버 내부 오류, 외부 API 오류</li>
 * </ul>
 *
 * <p>{@code @Getter}: getStatus(), getMessage()를 Lombok이 만들어 준다.
 * {@code @RequiredArgsConstructor}: final 필드(status, message)를 받는 생성자를 만들어 준다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // ---- 공통 ----
  /** 형식이 틀린 요청 (필수 값 누락, 숫자 자리에 문자 등) */
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  /** 없는 주소로 요청 */
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
  /** 예상하지 못한 서버 오류 */
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

  // ---- 지수 정보 (A) ----
  INDEX_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "지수 정보를 찾을 수 없습니다."),
  /** (지수 분류명 + 지수명)이 이미 등록되어 있음 */
  DUPLICATE_INDEX_INFO(HttpStatus.BAD_REQUEST, "이미 존재하는 지수입니다."),

  // ---- 자동 연동 설정 (A) ----
  AUTO_SYNC_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "자동 연동 설정을 찾을 수 없습니다."),

  // ---- 지수 데이터 (B) ----
  // 지금은 B 코드가 IllegalArgumentException을 쓰고 있어서 400으로 처리된다.
  // 아래 코드로 바꾸면 "없음"은 404로 정확하게 응답할 수 있다.
  INDEX_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "지수 데이터를 찾을 수 없습니다."),
  /** (지수 + 기준 일자)가 이미 등록되어 있음 */
  DUPLICATE_INDEX_DATA(HttpStatus.BAD_REQUEST, "이미 등록된 지수 데이터입니다.");

  // ---- 연동 (C), 대시보드 (D) ----
  // 필요한 에러를 위 목록 마지막 항목 뒤에 추가하면 된다. (마지막 항목 끝은 ; 로 끝나야 한다)

  /** 응답에 쓸 HTTP 상태 코드 */
  private final HttpStatus status;

  /** 사용자에게 보여 줄 기본 메시지 */
  private final String message;
}
