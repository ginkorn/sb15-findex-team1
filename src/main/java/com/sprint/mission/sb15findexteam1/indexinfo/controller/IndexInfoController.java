package com.sprint.mission.sb15findexteam1.indexinfo.controller;

import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.service.IndexInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 지수 정보 API. 경로·상태 코드는 Swagger 명세와 같다. */
@Tag(name = "지수 정보 API", description = "지수 정보 관리 API")
@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  @Operation(summary = "지수 정보 등록")
  @PostMapping
  public ResponseEntity<IndexInfoDto> create(@RequestBody @Valid IndexInfoCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(indexInfoService.create(request)); // 201
  }

  @Operation(summary = "지수 정보 목록 조회")
  @GetMapping
  public CursorPageResponseDto<IndexInfoDto> search(@ModelAttribute IndexInfoSearchCondition condition) {
    return indexInfoService.search(condition);
  }

  // "/summaries"는 "/{id}"보다 구체적인 주소라 먼저 매칭된다
  @Operation(summary = "지수 정보 요약 목록 조회")
  @GetMapping("/summaries")
  public List<IndexInfoSummaryDto> getSummaries() {
    return indexInfoService.getSummaries();
  }

  @Operation(summary = "지수 정보 조회")
  @GetMapping("/{id}")
  public IndexInfoDto get(@PathVariable Long id) {
    return indexInfoService.get(id);
  }

  @Operation(summary = "지수 정보 수정")
  @PatchMapping("/{id}")
  public IndexInfoDto update(@PathVariable Long id, @RequestBody @Valid IndexInfoUpdateRequest request) {
    return indexInfoService.update(id, request);
  }

  @Operation(summary = "지수 정보 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexInfoService.delete(id);
    return ResponseEntity.noContent().build(); // 204
  }
}
