package com.sprint.mission.sb15findexteam1.syncjob.controller;

import com.sprint.mission.sb15findexteam1.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobDto;
import com.sprint.mission.sb15findexteam1.syncjob.service.SyncJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자가 직접 실행하는 지수 정보·지수 데이터 연동 API.
 * 요청자의 원격 주소를 worker로 전달하고 실제 연동과 이력 저장은 서비스에 맡긴다.
 */
@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {
  private final SyncJobService syncJobService;

  /** 최신 지수 정보를 연동하고 항목별 작업 결과를 반환한다. */
  @Operation(summary = "지수 정보 연동")
  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> createInfos(HttpServletRequest httpRequest) {
    return ResponseEntity.ok(syncJobService.syncIndexInfo(httpRequest.getRemoteAddr()));
  }

  /** 요청한 지수와 기간의 데이터를 연동하고 항목별 작업 결과를 반환한다. */
  @Operation(summary = "지수 데이터 연동")
  @PostMapping("/index-data")
  public ResponseEntity<List<SyncJobDto>> createData(
      @RequestBody @Valid IndexDataSyncRequest request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(syncJobService.syncIndexData(request, httpRequest.getRemoteAddr()));
  }

}
