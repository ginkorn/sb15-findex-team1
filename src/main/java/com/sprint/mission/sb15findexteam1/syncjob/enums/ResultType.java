package com.sprint.mission.sb15findexteam1.syncjob.enums;

/**
 * 연동 작업(SyncJob)의 결과.
 *
 * <p>Swagger 명세의 result 값과 글자가 정확히 같아야 한다.
 * 실패도 반드시 FAILED로 기록해야 연동 이력 화면에서 확인할 수 있다.
 */
public enum ResultType {

  /** 연동 성공 */
  SUCCESS,

  /** 연동 실패 (Open API 오류, 데이터 없음 등) */
  FAILED
}
