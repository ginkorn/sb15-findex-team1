package com.sprint.mission.sb15findexteam1.common.enums;

/**
 * 데이터가 어디서 들어왔는지(출처)를 나타내는 값.
 *
 * <p>지수 정보(IndexInfo)와 지수 데이터(IndexData)가 함께 사용하기 때문에
 * 특정 도메인 패키지가 아닌 common/enums에 둔다.
 *
 * <p>DB에는 0, 1 같은 숫자가 아니라 "USER", "OPEN_API" 글자 그대로 저장된다.
 * (엔티티 쪽에서 {@code @Enumerated(EnumType.STRING)}으로 지정)
 */
public enum SourceType {

  /** 사용자가 화면(API)에서 직접 등록·수정한 데이터 */
  USER,

  /** 금융위원회 Open API 연동으로 자동 등록·수정된 데이터 */
  OPEN_API
}
