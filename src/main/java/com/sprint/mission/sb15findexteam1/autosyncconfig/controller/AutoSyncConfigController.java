package com.sprint.mission.sb15findexteam1.autosyncconfig.controller;

import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigDto;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigSearchCondition;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.sb15findexteam1.autosyncconfig.service.AutoSyncConfigService;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 자동 연동 설정 API. 프론트엔드의 "연동 관리" 화면에서 지수별 자동 연동 스위치를 보여 주고 켜고 끈다.
 * 어노테이션의 의미는 IndexInfoController 주석을 참고.
 *
 * <p>설정을 "만드는" API는 없다. 지수를 등록하면 IndexInfoService가 꺼진 상태로 자동으로 만든다.
 */
@Tag(name = "자동 연동 설정 API", description = "지수별 자동 연동 on/off")
@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {

  private final AutoSyncConfigService autoSyncConfigService;

  /**
   * 목록 조회. GET /api/auto-sync-configs?enabled=true&size=10 → 200
   * 쿼리 파라미터는 AutoSyncConfigSearchCondition에 담긴다.
   */
  @Operation(summary = "자동 연동 설정 목록 조회")
  @GetMapping
  public CursorPageResponseDto<AutoSyncConfigDto> search(@ModelAttribute AutoSyncConfigSearchCondition condition) {
    return autoSyncConfigService.search(condition);
  }

  /**
   * 켜기/끄기. PATCH /api/auto-sync-configs/{id} → 200
   * 요청 본문: {@code {"enabled": true}}. enabled가 없으면 400.
   */
  @Operation(summary = "자동 연동 설정 수정")
  @PatchMapping("/{id}")
  public AutoSyncConfigDto update(@PathVariable Long id,
      @RequestBody @Valid AutoSyncConfigUpdateRequest request) {
    return autoSyncConfigService.updateEnabled(id, request);
  }
}
