package com.sprint.mission.sb15findexteam1.dashboard.service;

import com.sprint.mission.sb15findexteam1.dashboard.calculator.FluctuationCalculator;
import com.sprint.mission.sb15findexteam1.dashboard.calculator.MovingAverageCalculator;
import com.sprint.mission.sb15findexteam1.dashboard.dto.ChartDataPointResponse;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexPerformanceDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.enums.PeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.mapper.ChartDataPointMapper;
import com.sprint.mission.sb15findexteam1.dashboard.repository.DashboardIndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final DashboardIndexDataRepository repository;
  private final MovingAverageCalculator movingAverageCalculator;
  private final ChartDataPointMapper chartDataPointMapper;
  private final FluctuationCalculator fluctuationCalculator;

  @Transactional(readOnly = true)     // 조회 전용 트랜잭션
  public IndexChartDto getChart(Long indexInfoId, ChartPeriodType periodType) {

    // 1) 이 지수의 가장 최근 데이터 날짜를 기준일(to)로 삼는다
    LocalDate to = repository.findTopByIndexInfoIdOrderByBaseDateDesc(indexInfoId)
        .orElseThrow()                  // 데이터가 없으면 예외 (자세한 처리는 나중 사이클)
        .getBaseDate();                 // 그 데이터의 날짜만 꺼낸다

    // 2) 기간 종류로 시작 날짜를 계산한다 (MONTHLY면 to - 1개월)
    LocalDate from = periodType.startDate(to);

    // 3) from ~ to 기간의 데이터를 날짜 오름차순으로 조회한다
    List<IndexData> rows = repository.findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(indexInfoId, from, to);

    // 4) 엔티티 목록을 차트용 (날짜, 종가) 목록으로 변환한다
    List<ChartDataPointResponse> dataPoints = chartDataPointMapper.toPoints(rows);

    // 4-1) 같은 종가 목록으로 이동평균을 계산한다 (window만 5, 20으로 다름)
    List<ChartDataPointResponse> ma5 = movingAverageCalculator.calculate(dataPoints, 5);
    List<ChartDataPointResponse> ma20 = movingAverageCalculator.calculate(dataPoints, 20);

    // 5) 응답 DTO를 조립한다
    return new IndexChartDto(
        indexInfoId,
        null,    // 지수 분류명 (IndexInfo 병합 후 채움)
        null,                    // 지수명 (IndexInfo 병합 후 채움)
        periodType.name(),
        dataPoints,              // 종가
        ma5,                     // 5일 이동평균
        ma20);                   // 20일 이동평균
  }

  // 지수 ID 목록의 성과를 입력 순서대로 계산한다. 데이터가 없는 지수는 건너뛴다
  @Transactional(readOnly = true)
  public List<IndexPerformanceDto> getPerformances(List<Long> indexInfoIds, PeriodType periodType) {
    return indexInfoIds.stream()                            // ID를 하나씩 꺼내서
        .map(id -> findPerformance(id, periodType))    // 각각 성과를 계산 (없으면 Optional.empty)
        .flatMap(Optional::stream)                          // 빈 것은 버리고, 값이 있는 것만 남김
        .toList();                                          // 목록으로 만들어 반환 (입력 순서 유지)
  }

  // 지수 1개의 성과를 계산한다. 데이터가 부족하면 예외 대신 Optional.empty()를 돌려준다
  private Optional<IndexPerformanceDto> findPerformance(Long indexInfoId, PeriodType periodType) {

    // 1) 가장 최근 데이터를 찾는다. 없으면 계산할 수 없으니 빈 값
    Optional<IndexData> current = repository.findTopByIndexInfoIdOrderByBaseDateDesc(indexInfoId);
    if (current.isEmpty()) {
      return Optional.empty();
    }

    // 2) 기간 유형으로 비교할 날짜를 계산한다
    LocalDate targetDate = periodType.baseDate(current.get().getBaseDate());

    // 3) 비교 날짜 이전(포함) 중 가장 최근 데이터를 찾는다. 없으면 빈 값
    Optional<IndexData> before = repository.findLatestOnOrBefore(indexInfoId, targetDate);
    if (before.isEmpty()) {
      return Optional.empty();
    }

    // 4) 두 종가로 대비, 등락률을 계산해서 결과를 Optional에 담아 돌려준다
    return Optional.of(fluctuationCalculator.calculate(
        indexInfoId,
        null,                  // 지수 분류명 (IndexInfo 병합 후 채움)
        null,                                  // 지수명 (IndexInfo 병합 후 채움)
        current.get().getClosingPrice(),       // 현재가
        before.get().getClosingPrice()));      // 기준가
  }
}