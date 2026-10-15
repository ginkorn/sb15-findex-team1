package com.sprint.mission.sb15findexteam1.dashboard.service;

import com.sprint.mission.sb15findexteam1.dashboard.calculator.MovingAverageCalculator;
import com.sprint.mission.sb15findexteam1.dashboard.dto.ChartDataPointResponse;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.mapper.ChartDataPointMapper;
import com.sprint.mission.sb15findexteam1.dashboard.repository.DashboardIndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor     // final 필드로 생성자를 자동 생성 (생성자 주입)
public class DashboardService {

  // 이 세 필드가 곧 생성자 파라미터이며, 선언 순서가 그대로 인자 순서가 된다
  private final DashboardIndexDataRepository repository;           // 조회용
  private final MovingAverageCalculator movingAverageCalculator;    // 이동평균 계산용 (다음 사이클에서 사용)
  private final ChartDataPointMapper chartDataPointMapper;          // 엔티티 → 차트 점 변환용

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
        null,                    // 지수 분류명 (IndexInfo 병합 후 채움)
        null,                    // 지수명 (IndexInfo 병합 후 채움)
        periodType.name(),
        dataPoints,              // 종가
        ma5,                     // 5일 이동평균   ← List.of() 대신
        ma20);                   // 20일 이동평균  ← List.of() 대신
  }
}