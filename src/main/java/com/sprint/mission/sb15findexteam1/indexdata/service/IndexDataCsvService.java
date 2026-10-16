
package com.sprint.mission.sb15findexteam1.indexdata.service;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.repository.IndexDataRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataCsvService {

  private final IndexDataRepository indexDataRepository;

  private static final String CSV_HEADER =
      "id,indexInfoId,baseDate,sourceType,"
          + "marketPrice,closingPrice,highPrice,lowPrice,"
          + "versus,fluctuationRate,tradingQuantity,"
          + "tradingPrice,marketTotalAmount";

  // 검색 조건에 맞는 지수 데이터를 CSV 파일 내용으로 변환
  public byte[] exportCsv(IndexDataSearchCondition condition) {

    // 필터와 정렬을 적용해 전체 데이터를 조회
    // CSV Export에는 페이지네이션을 적용하지 않음
    List<IndexData> indexDataList =
        indexDataRepository.findForCsv(condition);

    // CSV 문자열을 만드는 객체
    StringBuilder csv = new StringBuilder();

    // 한글이 포함된 CSV도 Excel에서 읽기 쉽도록 BOM 추가
    csv.append('\uFEFF');

    // CSV 첫 줄에 열 제목 추가
    csv.append(CSV_HEADER).append("\r\n");

    // 조회한 데이터를 한 줄씩 CSV에 추가
    for (IndexData data : indexDataList) {
      appendRow(csv, data);
    }

    // 완성된 CSV를 UTF-8 바이트 배열로 변환
    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }

  // IndexData 한 개를 CSV 한 줄로 변환
  private void appendRow(StringBuilder csv, IndexData data) {

    appendValues(
        csv,
        data.getId(),
        data.getIndexInfoId(),
        data.getBaseDate(),
        data.getSourceType(),
        data.getMarketPrice(),
        data.getClosingPrice(),
        data.getHighPrice(),
        data.getLowPrice(),
        data.getVersus(),
        data.getFluctuationRate(),
        data.getTradingQuantity(),
        data.getTradingPrice(),
        data.getMarketTotalAmount()
    );
  }

  // 여러 필드 값을 쉼표로 구분하여 CSV 한 줄 생성
  private void appendValues(StringBuilder csv, Object... values) {

    for (int i = 0; i < values.length; i++) {

      // 첫 번째 값이 아니라면 쉼표 추가
      if (i > 0) {
        csv.append(",");
      }

      csv.append(escapeCsv(values[i]));
    }

    csv.append("\r\n");
  }

  // CSV에서 특수문자가 안전하게 표현되도록 처리
  private String escapeCsv(Object value) {

    // DB 값이 null이면 빈 칸으로 처리
    if (value == null) {
      return "";
    }

    // BigDecimal은 소수점 값을 일반 숫자 문자열로 변환
    String text = value instanceof BigDecimal decimal
        ? decimal.toPlainString()
        : value.toString();

    // 큰따옴표는 두 개로 바꾸어 CSV 규칙에 맞춤
    String escaped = text.replace("\"", "\"\"");

    // 쉼표, 큰따옴표, 줄바꿈이 있다면 큰따옴표로 감싸기
    if (text.contains(",")
        || text.contains("\"")
        || text.contains("\n")
        || text.contains("\r")) {

      return "\"" + escaped + "\"";
    }

    return escaped;
  }
}
