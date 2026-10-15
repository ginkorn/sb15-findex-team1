package com.sprint.mission.sb15findexteam1.indexdata.controller;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataCsvService;
import com.sprint.mission.sb15findexteam1.indexdata.service.IndexDataService;
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

// 지수 데이터 등록, 조회, 수정, 삭제 및 CSV 다운로드 API
@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  // 지수 데이터 CRUD를 처리하는 서비스
  private final IndexDataService indexDataService;

  // CSV 파일을 생성하는 서비스
  private final IndexDataCsvService indexDataCsvService;


  // 지수 데이터 등록 API
  // POST /api/index-data
  @PostMapping
  public ResponseEntity<IndexDataDto> create(
      @RequestBody IndexDataCreateRequest request
  ) {

    // 요청 데이터를 DB에 저장
    IndexDataDto response = indexDataService.create(request);

    // 등록 성공 시 HTTP 201과 등록 결과 반환
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }


  // 지수 데이터 수정 API
  // PATCH /api/index-data/{id}
  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataDto> update(
      @PathVariable Long id,
      @RequestBody IndexDataUpdateRequest request
  ) {

    // 전달된 ID의 지수 데이터 수정
    IndexDataDto response = indexDataService.update(id, request);

    // 수정 성공 시 HTTP 200과 수정 결과 반환
    return ResponseEntity.ok(response);
  }


  // 지수 데이터 삭제 API
  // DELETE /api/index-data/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable Long id
  ) {

    // 전달된 ID의 지수 데이터 삭제
    indexDataService.delete(id);

    // 삭제 성공 시 HTTP 204 반환
    return ResponseEntity.noContent().build();
  }


  // 지수 데이터 CSV 다운로드 API
  // GET /api/index-data/export/csv
  @GetMapping("/export/csv")
  public ResponseEntity<byte[]> exportCsv(

      // 특정 지수 ID 필터 (선택)
      @RequestParam(required = false)
      Long indexInfoId,

      // 조회 시작 날짜 (선택)
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,

      // 조회 종료 날짜 (선택)
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate,

      // 정렬 기준 (선택)
      @RequestParam(required = false)
      String sortField,

      // 정렬 방향 (선택)
      @RequestParam(required = false)
      String sortDirection
  ) {

    // CSV 다운로드에 사용할 검색 조건 생성
    // CSV는 페이지네이션을 적용하지 않으므로
    // idAfter, cursor, size는 null로 전달
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

    // 검색 조건에 맞는 CSV 파일 내용 생성
    byte[] csv = indexDataCsvService.exportCsv(condition);

    // 파일 다운로드를 위한 HTTP 응답 생성
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


  // TODO(A): CursorPageResponse 규격 확정 후
  // GET /api/index-data 목록 조회 API 연결
  //
  // 현재 Service.findAll()은 List<IndexDataDto>를 반환하지만,
  // 프론트엔드는 커서 페이지 응답 객체를 요구하므로
  // 응답 구조를 완성한 후 구현해야 함

  // TODO(A): IndexInfo 연관관계 및 공통 예외 처리 연결

  // TODO(B): 요청 DTO의 @Valid 검증 적용
}
