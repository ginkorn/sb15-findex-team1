package com.sprint.mission.sb15findexteam1.syncjob.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 지수 데이터 연동 요청. (POST /api/sync-jobs/index-data)
 *
 * <p>indexInfoIds가 비어 있으면 모든 지수가 대상이다.
 * <p>TODO: Swagger 타입에 맞추기 (indexInfoIds → Long 목록, 날짜 → LocalDate)
 */
public record IndexDataSyncRequest(
    List<Integer> indexInfoIds,

    @NotNull
    String baseDateFrom,

    @NotNull
    String baseDateTo
) {

}
