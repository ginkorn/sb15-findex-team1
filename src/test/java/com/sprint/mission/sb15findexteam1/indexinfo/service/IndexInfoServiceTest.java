package com.sprint.mission.sb15findexteam1.indexinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.sb15findexteam1.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import com.sprint.mission.sb15findexteam1.common.exception.BusinessException;
import com.sprint.mission.sb15findexteam1.common.exception.ErrorCode;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.repository.IndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.repository.IndexInfoRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지수 정보 서비스 테스트. 노션 요구사항의 핵심 규칙이 지켜지는지 확인한다.
 *
 * <ul>
 *   <li>{@code @SpringBootTest}: 실제 서버처럼 스프링 전체를 띄우고 H2 DB로 테스트한다.</li>
 *   <li>{@code @Transactional}: 각 테스트가 끝나면 넣은 데이터를 모두 되돌린다. (테스트끼리 영향 없음)</li>
 *   <li>{@code @DisplayName}: 테스트 결과 화면에 보이는 한글 설명</li>
 * </ul>
 *
 * <p>실행: IntelliJ에서 클래스 옆 초록 삼각형, 또는 {@code ./gradlew test --tests "*IndexInfoServiceTest"}
 */
@SpringBootTest
@Transactional // 테스트가 끝나면 데이터를 되돌린다
class IndexInfoServiceTest {

  @Autowired IndexInfoService indexInfoService;
  @Autowired IndexInfoRepository indexInfoRepository;
  @Autowired AutoSyncConfigRepository autoSyncConfigRepository;
  @Autowired IndexDataRepository indexDataRepository;
  @Autowired EntityManager entityManager;

  private IndexInfoCreateRequest request(String classification, String name, int count) {
    return new IndexInfoCreateRequest(classification, name, count,
        LocalDate.of(2020, 1, 1), new BigDecimal("100"), false);
  }

  @Test
  @DisplayName("등록하면 출처는 USER이고, 자동 연동 설정이 꺼진 상태로 함께 생긴다")
  void create_withDisabledAutoSyncConfig() {
    IndexInfoDto created = indexInfoService.create(request("KOSPI시리즈", "코스피", 800));

    assertThat(created.sourceType()).isEqualTo(SourceType.USER);
    assertThat(autoSyncConfigRepository.findAll())
        .singleElement()
        .satisfies(config -> {
          assertThat(config.getIndexInfo().getId()).isEqualTo(created.id());
          assertThat(config.isEnabled()).isFalse();
        });
  }

  @Test
  @DisplayName("같은 분류명 + 지수명은 두 번 등록할 수 없다")
  void create_duplicate() {
    indexInfoService.create(request("KOSPI시리즈", "코스피", 800));

    assertThatThrownBy(() -> indexInfoService.create(request("KOSPI시리즈", "코스피", 900)))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_INDEX_INFO);
  }

  @Test
  @DisplayName("수정은 보낸 값만 바꾸고 나머지는 유지한다")
  void update_onlyGivenFields() {
    IndexInfoDto created = indexInfoService.create(request("KOSPI시리즈", "코스피", 800));

    IndexInfoDto updated = indexInfoService.update(created.id(),
        new IndexInfoUpdateRequest(null, null, null, true));

    assertThat(updated.favorite()).isTrue();
    assertThat(updated.employedItemsCount()).isEqualTo(800);
    assertThat(updated.baseIndex()).isEqualByComparingTo("100");
  }

  @Test
  @DisplayName("삭제하면 지수 데이터와 자동 연동 설정도 함께 삭제된다")
  void delete_removesRelatedData() {
    IndexInfoDto created = indexInfoService.create(request("KOSPI시리즈", "코스피", 800));
    indexDataRepository.save(IndexData.builder()
        .indexInfoId(created.id())
        .baseDate(LocalDate.of(2026, 10, 13))
        .sourceType(SourceType.USER)
        .closingPrice(new BigDecimal("2600.1234"))
        .build());

    indexInfoService.delete(created.id());
    // 자동 연동 설정은 DB의 ON DELETE CASCADE로 지워진다.
    // 테스트는 한 트랜잭션 안이라, 삭제 SQL을 DB로 먼저 보내야(flush) DB 규칙이 실행된다.
    entityManager.flush();

    assertThat(indexInfoRepository.findById(created.id())).isEmpty();
    assertThat(indexDataRepository.count()).isZero();
    assertThat(autoSyncConfigRepository.count()).isZero();
  }

  @Test
  @DisplayName("없는 지수를 조회하면 INDEX_INFO_NOT_FOUND")
  void get_notFound() {
    assertThatThrownBy(() -> indexInfoService.get(9999L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.INDEX_INFO_NOT_FOUND);
  }

  @Test
  @DisplayName("정렬 값이 모두 같아도 커서로 끝까지 넘기면 빠지거나 겹치는 항목이 없다")
  void search_cursorCoversAllWithoutDuplicates() {
    for (int i = 0; i < 25; i++) {
      indexInfoService.create(request("같은분류", "지수" + i, 10));
    }

    Set<Long> seen = new HashSet<>();
    List<Integer> pageSizes = new ArrayList<>();
    String cursor = null;
    Long idAfter = null;
    CursorPageResponseDto<IndexInfoDto> page;
    do {
      page = indexInfoService.search(new IndexInfoSearchCondition(
          null, null, null, idAfter, cursor, "indexClassification", "asc", 10));
      page.content().forEach(dto -> assertThat(seen.add(dto.id())).as("중복 없음").isTrue());
      pageSizes.add(page.size());
      cursor = page.nextCursor();
      idAfter = page.nextIdAfter();
    } while (page.hasNext());

    assertThat(seen).hasSize(25);
    assertThat(pageSizes).containsExactly(10, 10, 5);
    assertThat(page.totalElements()).isEqualTo(25);
  }

  @Test
  @DisplayName("필터: 지수명 부분 일치 + 즐겨찾기 일치, 숫자 정렬 내림차순")
  void search_filterAndNumericSort() {
    indexInfoService.create(request("KOSPI시리즈", "코스피", 800));
    indexInfoService.create(request("KOSPI시리즈", "코스피 200", 200));
    indexInfoService.create(request("KOSDAQ시리즈", "코스닥", 1500));
    IndexInfoDto favorite = indexInfoService.create(request("KRX시리즈", "KRX 300", 300));
    indexInfoService.update(favorite.id(), new IndexInfoUpdateRequest(null, null, null, true));

    CursorPageResponseDto<IndexInfoDto> byName = indexInfoService.search(new IndexInfoSearchCondition(
        null, "코스", null, null, null, "employedItemsCount", "desc", 10));
    assertThat(byName.content()).extracting(IndexInfoDto::indexName)
        .containsExactly("코스닥", "코스피", "코스피 200");

    CursorPageResponseDto<IndexInfoDto> byFavorite = indexInfoService.search(new IndexInfoSearchCondition(
        null, null, true, null, null, null, null, null));
    assertThat(byFavorite.content()).extracting(IndexInfoDto::indexName).containsExactly("KRX 300");
  }

  @Test
  @DisplayName("upsert: 없으면 OPEN_API로 등록, 있으면 값만 갱신하고 즐겨찾기는 유지")
  void upsertFromOpenApi() {
    IndexInfo first = indexInfoService.upsertFromOpenApi("KOSPI시리즈", "코스피", 800,
        LocalDate.of(1980, 1, 4), new BigDecimal("100"));
    indexInfoService.update(first.getId(), new IndexInfoUpdateRequest(null, null, null, true));

    IndexInfo second = indexInfoService.upsertFromOpenApi("KOSPI시리즈", "코스피", 810,
        LocalDate.of(1980, 1, 4), new BigDecimal("100"));

    assertThat(second.getId()).isEqualTo(first.getId());
    assertThat(second.getEmployedItemsCount()).isEqualTo(810);
    assertThat(second.getSourceType()).isEqualTo(SourceType.OPEN_API);
    assertThat(second.isFavorite()).isTrue();
    assertThat(autoSyncConfigRepository.count()).isEqualTo(1);
  }
}
