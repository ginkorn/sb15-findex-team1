package com.sprint.mission.sb15findexteam1.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;     // AssertJ (Hamcrest 아님)
import static org.mockito.BDDMockito.given;

import com.sprint.mission.sb15findexteam1.dashboard.calculator.FluctuationCalculator;
import com.sprint.mission.sb15findexteam1.dashboard.calculator.MovingAverageCalculator;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexPerformanceDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.enums.PeriodType;
import com.sprint.mission.sb15findexteam1.dashboard.mapper.ChartDataPointMapperImpl;
import com.sprint.mission.sb15findexteam1.dashboard.repository.DashboardIndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)       // Mockito를 이 테스트에서 쓰겠다는 설정
class DashboardServiceTest {

  @Mock                                            // 가짜 Repository (DB에 접속하지 않음)
  private DashboardIndexDataRepository repository;

  private DashboardService service;                // 테스트 대상

  @BeforeEach                                      // 각 테스트 시작 전에 매번 실행
  void setUp() {
    service = new DashboardService(
        repository,                                // 가짜 Repository
        new MovingAverageCalculator(),             // 진짜 계산 클래스
        new ChartDataPointMapperImpl(),          // MapStruct가 만든 진짜 구현체 (Service 필드 순서와 같게)
        new FluctuationCalculator());
  }

  // IndexData 엔티티를 간단히 만드는 도우미 (날짜, 종가만 의미 있음)
  private IndexData data(LocalDate date, String closing) {
    return IndexData.builder()
        .indexInfoId(1L)
        .baseDate(date)
        .sourceType("USER")
        .closingPrice(new BigDecimal(closing))
        .build();
  }

  // 지수 번호까지 지정해서 IndexData를 만드는 도우미 (여러 지수를 다루는 테스트용)
  private IndexData data(Long indexInfoId, LocalDate date, String closing) {
    return IndexData.builder()
        .indexInfoId(indexInfoId)       // 몇 번 지수의 데이터인지
        .baseDate(date)
        .sourceType("USER")
        .closingPrice(new BigDecimal(closing))
        .build();
  }

  @Test
  @DisplayName("차트는 기간 내 종가를 날짜 오름차순 dataPoints로 돌려준다")
  void getChart_dataPoints() {
    // given: 질문 ① "1번 지수의 가장 최근 데이터는?" → 2/14 데이터
    LocalDate to = LocalDate.of(2025, 2, 14);
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L))
        .willReturn(Optional.of(data(to, "120")));

    // given: 질문 ② "1번 지수의 1/14~2/14 데이터는?" → 3건
    LocalDate from = LocalDate.of(2025, 1, 14);
    given(repository.findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(1L, from, to))
        .willReturn(List.of(
            data(LocalDate.of(2025, 1, 14), "100"),
            data(LocalDate.of(2025, 1, 15), "110"),
            data(to, "120")));

    // when: 지수 1번의 1달 차트를 요청한다
    IndexChartDto result = service.getChart(1L, ChartPeriodType.MONTHLY);

    // then: 종가 3개가 날짜 오름차순으로 dataPoints에 담긴다
    assertThat(result.dataPoints()).hasSize(3);
    assertThat(result.dataPoints().get(0).date()).isEqualTo(LocalDate.of(2025, 1, 14));
    assertThat(result.dataPoints().get(0).value()).isEqualByComparingTo("100");
    assertThat(result.dataPoints().get(2).value()).isEqualByComparingTo("120");
  }

  @Test
  @DisplayName("차트는 종가로 계산한 5일·20일 이동평균을 돌려준다")
  void getChart_movingAverages() {
    // given: 이 지수의 마지막 데이터 날짜는 2025-01-19
    LocalDate to = LocalDate.of(2025, 1, 19);
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L))
        .willReturn(Optional.of(data(to, "106")));

    // MONTHLY의 시작일은 to - 1개월 = 2024-12-19
    LocalDate from = LocalDate.of(2024, 12, 19);

    // 종가 6개를 날짜 오름차순으로 반환 (100, 102, 101, 103, 104, 106)
    given(repository.findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(1L, from, to))
        .willReturn(List.of(
            data(LocalDate.of(2025, 1, 14), "100"),
            data(LocalDate.of(2025, 1, 15), "102"),
            data(LocalDate.of(2025, 1, 16), "101"),
            data(LocalDate.of(2025, 1, 17), "103"),
            data(LocalDate.of(2025, 1, 18), "104"),
            data(LocalDate.of(2025, 1, 19), "106")));

    // when
    IndexChartDto result = service.getChart(1L, ChartPeriodType.MONTHLY);

    // then: 5일 평균은 5번째 날(1/18)부터 2개가 나온다
    //   1/18 = (100+102+101+103+104) / 5 = 102.00
    //   1/19 = (102+101+103+104+106) / 5 = 103.20
    assertThat(result.ma5DataPoints()).hasSize(2);
    assertThat(result.ma5DataPoints().get(0).date()).isEqualTo(LocalDate.of(2025, 1, 18));
    assertThat(result.ma5DataPoints().get(0).value()).isEqualByComparingTo("102.00");
    assertThat(result.ma5DataPoints().get(1).value()).isEqualByComparingTo("103.20");

    // 20일 평균은 데이터가 6개뿐이라 만들 수 없다
    assertThat(result.ma20DataPoints()).isEmpty();
  }

  @Test
  @DisplayName("성과: 현재가와 기준가로 대비와 등락률을 계산한다")
  void getPerformance_daily() {
    // given: 1번 지수의 가장 최근 데이터는 1/15, 종가 110
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L))
        .willReturn(Optional.of(data(LocalDate.of(2025, 1, 15), "110")));

    // given: DAILY의 비교 날짜는 하루 전인 1/14, 그날 이전 최근 데이터는 1/14, 종가 100
    given(repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 1, 14)))
        .willReturn(Optional.of(data(LocalDate.of(2025, 1, 14), "100")));

    // when
    IndexPerformanceDto result = service.getPerformances(List.of(1L), PeriodType.DAILY).get(0);

    // then: 현재 110, 이전 100 → 대비 +10, 등락률 +10.00%
    assertThat(result.indexInfoId()).isEqualTo(1L);
    assertThat(result.currentPrice()).isEqualByComparingTo("110");
    assertThat(result.beforePrice()).isEqualByComparingTo("100");
    assertThat(result.versus()).isEqualByComparingTo("10");
    assertThat(result.fluctuationRate()).isEqualByComparingTo("10.00");
  }

  @Test
  @DisplayName("성과: 비교 날짜에 데이터가 없으면 그 이전 가장 가까운 날의 종가를 기준가로 쓴다")
  void getPerformance_weeklyWithGap() {
    // given: 가장 최근 데이터는 1/15, 종가 110
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L))
        .willReturn(Optional.of(data(LocalDate.of(2025, 1, 15), "110")));

    // given: WEEKLY의 비교 날짜는 7일 전인 1/8
    //        그날은 데이터가 없어서, Repository가 이전 중 가장 가까운 1/7(종가 105)을 돌려준다고 가정
    given(repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 1, 8)))
        .willReturn(Optional.of(data(LocalDate.of(2025, 1, 7), "105")));

    // when
    IndexPerformanceDto result = service.getPerformances(List.of(1L), PeriodType.WEEKLY).get(0);

    // then: 현재 110, 이전 105 → 대비 +5, 등락률 5 ÷ 105 × 100 = 4.7619... → 4.76
    assertThat(result.beforePrice()).isEqualByComparingTo("105");
    assertThat(result.versus()).isEqualByComparingTo("5");
    assertThat(result.fluctuationRate()).isEqualByComparingTo("4.76");
  }

  @Test
  @DisplayName("여러 지수의 성과를 입력한 순서대로 계산해서 돌려준다")
  void getPerformances_multiple() {
    // given: 1번 지수 - 현재(1/15) 110, 하루 전(1/14) 100 (오른 경우)
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L))
        .willReturn(Optional.of(data(1L, LocalDate.of(2025, 1, 15), "110")));
    given(repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 1, 14)))
        .willReturn(Optional.of(data(1L, LocalDate.of(2025, 1, 14), "100")));

    // given: 2번 지수 - 현재(1/15) 200, 하루 전(1/14) 250 (내린 경우)
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(2L))
        .willReturn(Optional.of(data(2L, LocalDate.of(2025, 1, 15), "200")));
    given(repository.findLatestOnOrBefore(2L, LocalDate.of(2025, 1, 14)))
        .willReturn(Optional.of(data(2L, LocalDate.of(2025, 1, 14), "250")));

    // when: 1번, 2번 지수의 일간 성과를 요청
    List<IndexPerformanceDto> result = service.getPerformances(List.of(1L, 2L), PeriodType.DAILY);

    // then: 2개가 입력 순서대로 나온다
    assertThat(result).hasSize(2);
    assertThat(result.get(0).indexInfoId()).isEqualTo(1L);
    assertThat(result.get(0).fluctuationRate()).isEqualByComparingTo("10.00");    // 100 → 110
    assertThat(result.get(1).indexInfoId()).isEqualTo(2L);
    assertThat(result.get(1).fluctuationRate()).isEqualByComparingTo("-20.00");   // 250 → 200
  }

  @Test
  @DisplayName("지수 ID 목록이 비어 있으면 빈 목록을 돌려준다")
  void getPerformances_empty() {
    // when: 즐겨찾기한 지수가 없는 경우
    List<IndexPerformanceDto> result = service.getPerformances(List.of(), PeriodType.DAILY);

    // then: 에러가 아니라 빈 목록 (프런트가 "즐겨찾기에 등록해주세요"를 보여 줌)
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("데이터가 없는 지수는 건너뛰고 나머지만 돌려준다")
  void getPerformances_skipNoData() {
    // given: 1번 지수는 정상 데이터가 있다
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L))
        .willReturn(Optional.of(data(1L, LocalDate.of(2025, 1, 15), "110")));
    given(repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 1, 14)))
        .willReturn(Optional.of(data(1L, LocalDate.of(2025, 1, 14), "100")));

    // given: 2번 지수는 데이터가 아예 없다
    given(repository.findTopByIndexInfoIdOrderByBaseDateDesc(2L))
        .willReturn(Optional.empty());

    // when
    List<IndexPerformanceDto> result = service.getPerformances(List.of(1L, 2L), PeriodType.DAILY);

    // then: 2번은 빠지고 1번만 남는다
    assertThat(result).hasSize(1);
    assertThat(result.get(0).indexInfoId()).isEqualTo(1L);
  }
}