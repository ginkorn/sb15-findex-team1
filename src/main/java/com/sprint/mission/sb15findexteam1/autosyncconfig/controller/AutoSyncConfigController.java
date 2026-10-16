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

@Tag(name = "자동 연동 설정 API", description = "지수별 자동 연동 on/off")
@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {

  private final AutoSyncConfigService autoSyncConfigService;

  @Operation(summary = "자동 연동 설정 목록 조회")
  @GetMapping
  public CursorPageResponseDto<AutoSyncConfigDto> search(@ModelAttribute AutoSyncConfigSearchCondition condition) {
    return autoSyncConfigService.search(condition);
  }

  @Operation(summary = "자동 연동 설정 수정")
  @PatchMapping("/{id}")
  public AutoSyncConfigDto update(@PathVariable Long id,
      @RequestBody @Valid AutoSyncConfigUpdateRequest request) {
    return autoSyncConfigService.updateEnabled(id, request);
  }
}
