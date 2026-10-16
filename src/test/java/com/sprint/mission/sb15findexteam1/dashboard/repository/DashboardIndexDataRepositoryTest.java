package com.sprint.mission.sb15findexteam1.dashboard.repository;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb15findexteam1.dashboard.repository.DashboardIndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest   // JPA 관련 빈만 띄우고 H2 메모리 DB를 사용하는 테스트 (서비스·컨트롤러는 안 띄움)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DashboardIndexDataRepositoryTest {

  @Autowired
  private DashboardIndexDataRepository repository;   // 아직 없어서 빨간 줄이 정상

  // 지수 1번의 데이터 3개(1/14, 1/15, 2/14)와, 다른 지수(2번)의 데이터 1개를 저장해 둔다
  @BeforeEach
  void setUp() {
    repository.save(data(1L, LocalDate.of(2025, 1, 14), "100"));
    repository.save(data(1L, LocalDate.of(2025, 1, 15), "110"));
    repository.save(data(1L, LocalDate.of(2025, 2, 14), "120"));
    repository.save(data(2L, LocalDate.of(2025, 1, 15), "999"));   // 다른 지수 (조회되면 안 됨)
  }

  // 테스트용 IndexData를 만드는 도우미 (종가와 날짜만 의미 있음)
  private IndexData data(Long indexInfoId, LocalDate date, String closing) {
    return IndexData.builder()
        .indexInfoId(indexInfoId)
        .baseDate(date)
        .sourceType(SourceType.USER)
        .closingPrice(new BigDecimal(closing))
        .build();
  }

  // ----- 방식 1: JPA 메서드 이름 -----
  @Test
  @DisplayName("[JPA] 기간 내 데이터를 날짜 오름차순으로 조회한다")
  void jpa_between() {
    List<IndexData> result = repository.findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
        1L, LocalDate.of(2025, 1, 14), LocalDate.of(2025, 2, 14));

    // 조회된 각 행의 날짜와 종가를 출력한다 (확인용)
    for (IndexData d : result) {
      System.out.println(d.getBaseDate() + " | 종가 " + d.getClosingPrice());
    }

    // 지수 1번의 3건만, 날짜 오름차순
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getBaseDate()).isEqualTo(LocalDate.of(2025, 1, 14));
    assertThat(result.get(2).getBaseDate()).isEqualTo(LocalDate.of(2025, 2, 14));
  }

  @Test
  @DisplayName("[JPA] 마지막 데이터 날짜를 조회한다")
  void jpa_latest() {
    Optional<IndexData> latest = repository.findTopByIndexInfoIdOrderByBaseDateDesc(1L);

    assertThat(latest).isPresent();
    assertThat(latest.get().getBaseDate()).isEqualTo(LocalDate.of(2025, 2, 14));
  }



  // 기준일 당일 데이터가 있으면 그 데이터를 돌려줘야 한다 (기준일 "포함")
  @Test
  @DisplayName("[QueryDSL] 기준일 당일 데이터가 있으면 당일 데이터를 조회한다")
  void findLatestOnOrBefore_sameDay() {
    // when: 2025-02-14 이전(포함) 중 가장 최근 데이터를 찾는다
    Optional<IndexData> result = repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 2, 14));

    // then: 2/14 데이터(종가 120)가 나와야 한다
    assertThat(result).isPresent();
    assertThat(result.get().getBaseDate()).isEqualTo(LocalDate.of(2025, 2, 14));
    assertThat(result.get().getClosingPrice()).isEqualByComparingTo("120");
  }

  // 기준일에 데이터가 없으면(주말, 휴일) 그 이전 중 가장 가까운 날을 돌려줘야 한다
  @Test
  @DisplayName("[QueryDSL] 기준일에 데이터가 없으면 이전 중 가장 가까운 날을 조회한다")
  void findLatestOnOrBefore_gap() {
    // when: 2025-02-01에는 데이터가 없다 (있는 날은 1/14, 1/15, 2/14)
    Optional<IndexData> result = repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 2, 1));

    // then: 2/1 이전 중 가장 가까운 1/15 데이터(종가 110)가 나와야 한다
    assertThat(result).isPresent();
    assertThat(result.get().getBaseDate()).isEqualTo(LocalDate.of(2025, 1, 15));
    assertThat(result.get().getClosingPrice()).isEqualByComparingTo("110");
  }

  // 기준일 이전에 데이터가 아예 없으면 빈 결과여야 한다
  @Test
  @DisplayName("[QueryDSL] 기준일 이전 데이터가 없으면 빈 결과를 돌려준다")
  void findLatestOnOrBefore_empty() {
    // when: 가장 오래된 데이터(1/14)보다 이전 날짜로 찾는다
    Optional<IndexData> result = repository.findLatestOnOrBefore(1L, LocalDate.of(2025, 1, 1));

    // then: 없으니 비어 있어야 한다
    assertThat(result).isEmpty();
  }
}
