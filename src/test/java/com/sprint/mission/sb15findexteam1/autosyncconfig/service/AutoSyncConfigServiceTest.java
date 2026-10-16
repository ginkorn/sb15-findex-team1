package com.sprint.mission.sb15findexteam1.autosyncconfig.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigDto;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigSearchCondition;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoDto;
import com.sprint.mission.sb15findexteam1.indexinfo.service.IndexInfoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자동 연동 설정 서비스 테스트.
 * 지수를 등록하면 설정이 자동으로 생기고, 목록·켜기·필터·배치용 조회가 동작하는지 확인한다.
 */
@SpringBootTest
@Transactional // 테스트가 끝나면 데이터를 되돌린다
class AutoSyncConfigServiceTest {

  @Autowired AutoSyncConfigService autoSyncConfigService;
  @Autowired IndexInfoService indexInfoService;

  private IndexInfoDto createIndex(String name) {
    return indexInfoService.create(new IndexInfoCreateRequest("KOSPI시리즈", name, 10,
        LocalDate.of(2020, 1, 1), new BigDecimal("100"), false));
  }

  @Test
  @DisplayName("목록에는 지수명이 함께 나오고, 켜기·필터·배치용 id 조회가 동작한다")
  void searchUpdateAndEnabledIds() {
    IndexInfoDto b = createIndex("나지수");
    IndexInfoDto a = createIndex("가지수");

    CursorPageResponseDto<AutoSyncConfigDto> all = autoSyncConfigService.search(
        new AutoSyncConfigSearchCondition(null, null, null, null, null, null, null));
    assertThat(all.content()).extracting(AutoSyncConfigDto::indexName).containsExactly("가지수", "나지수");
    assertThat(all.content()).allMatch(dto -> !dto.enabled());

    Long configOfB = all.content().get(1).id();
    AutoSyncConfigDto updated = autoSyncConfigService.updateEnabled(configOfB, new AutoSyncConfigUpdateRequest(true));
    assertThat(updated.enabled()).isTrue();
    assertThat(updated.indexInfoId()).isEqualTo(b.id());

    CursorPageResponseDto<AutoSyncConfigDto> enabledOnly = autoSyncConfigService.search(
        new AutoSyncConfigSearchCondition(null, true, null, null, null, null, null));
    assertThat(enabledOnly.content()).extracting(AutoSyncConfigDto::indexName).containsExactly("나지수");

    assertThat(autoSyncConfigService.findEnabledIndexInfoIds()).containsExactly(b.id());
    assertThat(a.id()).isNotEqualTo(b.id());
  }
}
