package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 지수 정보 응답. 필드 이름은 Swagger의 IndexInfoDto와 같다. */
public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    Boolean favorite
) {
}
