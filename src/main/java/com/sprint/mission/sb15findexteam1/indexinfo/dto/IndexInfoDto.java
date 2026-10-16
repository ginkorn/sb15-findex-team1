package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 응답 = 등록·조회·수정·목록 API가 돌려주는 지수 하나의 모양.
 * 필드 이름은 Swagger의 IndexInfoDto와 같다.
 *
 * <p>왜 엔티티(IndexInfo)를 그대로 응답하지 않나?
 * 엔티티를 그대로 내보내면 DB 구조가 API에 묶여서 테이블을 고칠 때 화면까지 깨지고,
 * 연관된 객체를 따라가다 에러(지연 로딩, 무한 순환)가 날 수 있다. 그래서 응답 전용 DTO로 바꿔서 보낸다.
 * 변환은 IndexInfoMapper(MapStruct)가 자동으로 한다.
 */
public record IndexInfoDto(
    Long id,                      // 지수 정보 id
    String indexClassification,   // 지수 분류명
    String indexName,             // 지수명
    Integer employedItemsCount,   // 채용 종목 수
    LocalDate basePointInTime,    // 기준 시점
    BigDecimal baseIndex,         // 기준 지수
    SourceType sourceType,        // 출처: "USER" 또는 "OPEN_API" (JSON에는 글자로 나간다)
    Boolean favorite              // 즐겨찾기
) {
}
