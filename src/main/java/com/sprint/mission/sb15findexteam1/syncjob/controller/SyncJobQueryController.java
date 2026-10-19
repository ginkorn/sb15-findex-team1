package com.sprint.mission.sb15findexteam1.syncjob.controller;

import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.service.SyncJobQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 연동 이력 목록 조회 API (C의 SyncJobController는 건드리지 않고, 같은 주소의 GET만 따로 담당한다)
@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobQueryController {

  private final SyncJobQueryService syncJobQueryService;

  // 연동 이력 목록 조회: 필터, 정렬, 커서 파라미터를 검색 조건으로 받아 서비스에 넘긴다
  @Operation(summary = "연동 작업 목록 조회")
  @GetMapping
  public CursorPageResponseDto<SyncJobDto> getSyncJobs(@ParameterObject SyncJobSearchCondition condition) {
    return syncJobQueryService.getSyncJobs(condition);     // 서비스 결과를 그대로 응답
  }
}