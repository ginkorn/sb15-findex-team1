package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 수정 요청 (PATCH /api/index-infos/{id}).
 *
 * <p>모두 선택 값이다. 보낸 값만 바뀌고, 보내지 않은(null) 값은 그대로 유지된다.
 * 프론트엔드는 분류명·지수명까지 함께 보내지만, 요구사항상 수정할 수 없어서 받지 않는다.
 * (모르는 필드는 Jackson이 무시하므로 에러가 나지 않는다)
 */
public record IndexInfoUpdateRequest(
    @PositiveOrZero Integer employedItemsCount,
    LocalDate basePointInTime,
    @Positive BigDecimal baseIndex,
    Boolean favorite
) {
}
