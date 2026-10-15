package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import java.math.BigDecimal;

public record IndexInfoDto(
    Long id,
    String indexClassification, // 지수분류명 / ex. KOSPI 시리즈
    String indexName,           // 지수명 / ex. IT 서비스
    Integer employedItemsCount,     // 채용 종목 수 -> 지수가 채용한 종목의 수 / ex. 25
    String basePointInTime,     // 지수를 산출하기 위한 기준시점 / ex. 20240701
    BigDecimal baseIndex,           // 기준 지수
    SourceType sourceType,
    boolean favorite            //
) {

}
