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

  @Transactional(readOnly = true)     // 조회 전용 트랜잭션
  public IndexPerformanceDto getPerformance(Long indexInfoId, PeriodType periodType) {

    // 1) 이 지수의 가장 최근 데이터를 찾는다 → 현재가의 기준
    IndexData current = repository.findTopByIndexInfoIdOrderByBaseDateDesc(indexInfoId).orElseThrow();

    // 2) 기간 유형으로 "비교할 날짜"를 계산한다 (DAILY면 하루 전, WEEKLY면 7일 전, MONTHLY면 한 달 전)
    LocalDate targetDate = periodType.baseDate(current.getBaseDate());

    // 3) 비교 날짜 이전(포함) 중 가장 최근 데이터를 찾는다 → 기준가 (주말, 휴일이면 직전 영업일)
    IndexData before = repository.findLatestOnOrBefore(indexInfoId, targetDate).orElseThrow();

    // 4) 현재 종가와 기준 종가로 대비, 등락률을 계산해 응답 DTO를 만든다
    return fluctuationCalculator.calculate(
        indexInfoId,
        null,                       // 지수 분류명 (IndexInfo 병합 후 채움)
        null,                       // 지수명 (IndexInfo 병합 후 채움)
        current.getClosingPrice(),  // 현재가
        before.getClosingPrice());  // 기준가
  }

}