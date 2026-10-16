
package com.sprint.mission.sb15findexteam1.indexdata.controller;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataCsvService;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataService;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 지수 데이터 등록, 수정, 삭제, CSV 다운로드를 처리하는 Controller
@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  // 지수 데이터 등록, 수정, 삭제를 담당하는 서비스
  private final IndexDataService indexDataService;

  // 지수 데이터 CSV 파일 생성을 담당하는 서비스
  private final IndexDataCsvService indexDataCsvService;

  // POST /api/index-data
  @Operation(summary = "지수 데이터 등록")
  @PostMapping
  public ResponseEntity<IndexDataDto> create(
      @RequestBody IndexDataCreateRequest request
  ) {

    // 요청받은 지수 데이터를 DB에 저장
    IndexDataDto response = indexDataService.create(request);

    // 등록 성공 시 HTTP 201 반환
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  // PATCH /api/index-data/{id}
  @Operation(summary = "지수 데이터 수정")
  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataDto> update(
      @PathVariable Long id,
      @RequestBody IndexDataUpdateRequest request
  ) {

    // ID로 기존 데이터를 찾아 요청받은 필드만 수정
    IndexDataDto response = indexDataService.update(id, request);

    // 수정 성공 시 HTTP 200 반환
    return ResponseEntity.ok(response);
  }

  // DELETE /api/index-data/{id}
  @Operation(summary = "지수 데이터 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable Long id
  ) {

    // ID에 해당하는 지수 데이터 삭제
    indexDataService.delete(id);

    // 삭제 성공 시 HTTP 204 반환
    return ResponseEntity.noContent().build();
  }

  // GET /api/index-data
  @Operation(summary = "지수 데이터 목록 조회")
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<IndexDataDto>> findAll(

      // 조회할 지수 정보 ID (선택)
      @RequestParam(required = false)
      Long indexInfoId,

      // 검색 시작 날짜 (선택)
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,

      // 검색 종료 날짜 (선택)
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate,

      // 다음 페이지 조회를 위한 커서 (선택)
      @RequestParam(required = false)
      String cursor,

      // 마지막으로 조회한 데이터 ID (선택)
      @RequestParam(required = false)
      Long idAfter,

      // 정렬할 필드 (선택)
      @RequestParam(required = false)
      String sortField,

      // 정렬 방향: asc 또는 desc (선택)
      @RequestParam(required = false)
      String sortDirection,

      // 한 페이지에 조회할 데이터 개수 (선택)
      @RequestParam(required = false)
      Integer size
  ) {

    // 요청받은 검색 조건을 DTO로 변환
    IndexDataSearchCondition condition =
        new IndexDataSearchCondition(
            indexInfoId,
            startDate,
            endDate,
            idAfter,
            cursor,
            sortField,
            sortDirection,
            size
        );

    // Service를 통해 목록 조회 및 페이지 정보 생성
    CursorPageResponseDto<IndexDataDto> response =
        indexDataService.findAll(condition);

    // 조회 성공 시 HTTP 200 반환
    return ResponseEntity.ok(response);
  }

  // GET /api/index-data/export/csv
  @Operation(summary = "지수 데이터 CSV 다운로드")
  @GetMapping("/export/csv")
  public ResponseEntity<byte[]> exportCsv(

      // 조회할 지수 정보 ID (선택)
      @RequestParam(required = false)
      Long indexInfoId,

      // 검색 시작 날짜 (선택)
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,

      // 검색 종료 날짜 (선택)
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate,

      // 정렬할 필드 (선택)
      @RequestParam(required = false)
      String sortField,

      // 정렬 방향: asc 또는 desc (선택)
      @RequestParam(required = false)
      String sortDirection
  ) {

    // CSV 조회에 필요한 검색 조건 생성
    // CSV에는 페이지네이션을 적용하지 않음
    IndexDataSearchCondition condition =
        new IndexDataSearchCondition(
            indexInfoId,
            startDate,
            endDate,
            null,
            null,
            sortField,
            sortDirection,
            null
        );

    // 검색 조건에 맞는 CSV 데이터 생성
    byte[] csv = indexDataCsvService.exportCsv(condition);

    // CSV 파일을 다운로드할 수 있도록 HTTP 응답 생성
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"index-data.csv\""
        )
        .contentType(
            MediaType.parseMediaType("text/csv;charset=UTF-8")
        )
        .body(csv);
  }

  // TODO(A): IndexInfo 연관관계 및 공통 예외 처리 연결
  // TODO(B): @Valid를 이용한 요청 데이터 유효성 검증
}
