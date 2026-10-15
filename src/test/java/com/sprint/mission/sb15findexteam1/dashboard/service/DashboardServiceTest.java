package com.sprint.mission.sb15findexteam1.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;     // AssertJ (Hamcrest 아님)
import static org.mockito.BDDMockito.given;

import com.sprint.mission.sb15findexteam1.dashboard.calculator.MovingAverageCalculator;
import com.sprint.mission.sb15findexteam1.dashboard.dto.IndexChartDto;
import com.sprint.mission.sb15findexteam1.dashboard.enums.ChartPeriodType;
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
        new ChartDataPointMapperImpl());           // MapStruct가 만든 진짜 구현체 (Service 필드 순서와 같게)
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
}