package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 수정 요청 = PATCH /api/index-infos/{id} 의 요청 본문(JSON).
 *
 * <p><b>모두 선택 값이다.</b> 보낸 값만 바뀌고, 보내지 않은(null) 값은 원래 값 그대로 남는다.
 * 예: {@code {"favorite": true}}만 보내면 즐겨찾기만 켜진다.
 *
 * <p>분류명·지수명은 노션 요구사항상 수정할 수 없어서 필드가 없다.
 * 프론트엔드는 분류명·지수명까지 함께 보내지만, 모르는 필드는 JSON 변환기(Jackson)가 무시하므로 에러가 나지 않는다.
 *
 * <p>값을 보냈다면 범위는 검사한다. (예: 채용 종목 수에 -1을 보내면 400)
 */
public record IndexInfoUpdateRequest(
    @PositiveOrZero Integer employedItemsCount, // 채용 종목 수
    LocalDate basePointInTime,                  // 기준 시점
    @Positive BigDecimal baseIndex,             // 기준 지수
    Boolean favorite                            // 즐겨찾기
) {
}
