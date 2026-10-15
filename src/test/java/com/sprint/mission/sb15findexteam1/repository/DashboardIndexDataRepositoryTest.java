package com.sprint.mission.sb15findexteam1.repository;

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
        .sourceType("USER")
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

  // ----- 방식 2: QueryDSL (3단계에서 구현 후 주석을 풀어 사용) -----
  // @Test
  // void querydsl_between() { repository.findPricesBetween(...) }
}
