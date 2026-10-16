package com.sprint.mission.sb15findexteam1.indexinfo.dto;

import com.sprint.mission.sb15findexteam1.openapi.dto.OpenApiResponseDto.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 지수 정보 등록 요청 = POST /api/index-infos 의 요청 본문(JSON).
 * 필드 이름은 Swagger의 IndexInfoCreateRequest와 같다. (프론트엔드가 이 이름으로 보낸다)
 *
 * <pre>
 * {
 *   "indexClassification": "KOSPI시리즈",
 *   "indexName": "코스피",
 *   "employedItemsCount": 800,
 *   "basePointInTime": "1980-01-04",
 *   "baseIndex": 100,
 *   "favorite": false
 * }
 * </pre>
 *
 * <p>필드 앞의 검증 어노테이션은 컨트롤러의 {@code @Valid}가 검사한다. 하나라도 틀리면 400으로 응답한다.
 * <ul>
 *   <li>{@code @NotBlank}: null, 빈 문자열 "", 공백 "  " 모두 안 됨 (문자열용)</li>
 *   <li>{@code @NotNull}: null만 안 됨 (숫자·날짜용)</li>
 *   <li>{@code @PositiveOrZero}: 0 이상, {@code @Positive}: 0보다 커야 함</li>
 * </ul>
 */
public record IndexInfoCreateRequest(
    @NotBlank String indexClassification,               // 지수 분류명 (필수)
    @NotBlank String indexName,                         // 지수명 (필수)
    @NotNull @PositiveOrZero Integer employedItemsCount, // 채용 종목 수 (필수, 0 이상)
    @NotNull LocalDate basePointInTime,                 // 기준 시점 (필수, "yyyy-MM-dd" 형식)
    @NotNull @Positive BigDecimal baseIndex,            // 기준 지수 (필수, 0보다 큼)
    Boolean favorite                                    // 즐겨찾기 (선택, 보내지 않으면 false로 저장)
) {

  private static final DateTimeFormatter KRX_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  /** Open API 응답을 지수 정보 DTO로 변환한다. */
  public static IndexInfoCreateRequest fromOpenApi(Item item) {
    return new IndexInfoCreateRequest(
        item.idxCsf(),
        item.idxNm(),
        Integer.valueOf(item.epyItmsCnt()),
        LocalDate.parse(item.basPntm(), KRX_DATE_FORMAT),
        new BigDecimal(item.basIdx()),
        false
    );
  }
}
