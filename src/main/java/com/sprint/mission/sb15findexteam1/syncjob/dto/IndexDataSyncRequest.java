package com.sprint.mission.sb15findexteam1.syncjob.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record IndexDataSyncRequest(
    List<Integer> indexInfoIds,

    @NotNull
    String baseDateFrom,

    @NotNull
    String baseDateTo
) {

}
