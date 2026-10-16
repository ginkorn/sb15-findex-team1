package com.sprint.mission.sb15findexteam1.autosyncconfig.dto;

import jakarta.validation.constraints.NotNull;

/** 자동 연동 설정 수정 요청 (PATCH /api/auto-sync-configs/{id}). 켜고 끄는 값 하나뿐이다. */
public record AutoSyncConfigUpdateRequest(
    @NotNull Boolean enabled
) {
}
