package com.sprint.mission.sb15findexteam1.syncjob.enums;

/**
 * 연동 작업(SyncJob)의 종류.
 *
 * <p>Swagger 명세의 jobType 값과 글자가 정확히 같아야 한다.
 * (프론트엔드가 이 글자로 화면에 표시하고 필터링한다)
 */
public enum JobType {

  /** 지수 정보 연동: Open API 응답에서 지수 목록을 뽑아 IndexInfo를 등록·수정 */
  INDEX_INFO,

  /** 지수 데이터 연동: 날짜별 시세를 받아 IndexData를 등록·수정 */
  INDEX_DATA
}
