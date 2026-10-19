package com.sprint.mission.sb15findexteam1.syncjob.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 지수 데이터 연동 요청. (POST /api/sync-jobs/index-data)
 *
 * <p>indexInfoIds가 null이거나 비어 있으면 모든 지수가 대상이다.
 * baseDateFrom과 baseDateTo는 모두 조회 범위에 포함된다.
 */
public record IndexDataSyncRequest(
    List<Long> indexInfoIds,

    @NotNull
    LocalDate baseDateFrom,

    @NotNull
    LocalDate baseDateTo
) {

}
