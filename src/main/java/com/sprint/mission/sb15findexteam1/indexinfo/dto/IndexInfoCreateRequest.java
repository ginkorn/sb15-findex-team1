package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 등록 요청 (POST /api/index-infos). 필드 이름은 Swagger의 IndexInfoCreateRequest와 같다.
 * favorite을 제외한 값은 모두 필수이며, 빠지면 GlobalExceptionHandler가 400으로 응답한다.
 */
public record IndexInfoCreateRequest(
    @NotBlank String indexClassification,      // 지수 분류명
    @NotBlank String indexName,                // 지수명
    @NotNull @PositiveOrZero Integer employedItemsCount, // 채용 종목 수
    @NotNull LocalDate basePointInTime,        // 기준 시점 (yyyy-MM-dd)
    @NotNull @Positive BigDecimal baseIndex,   // 기준 지수
    Boolean favorite                           // 즐겨찾기 (보내지 않으면 false)
) {
}
