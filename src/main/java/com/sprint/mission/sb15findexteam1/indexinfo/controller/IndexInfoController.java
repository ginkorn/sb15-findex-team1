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

/**
 * 지수 정보 API. 주소·상태 코드는 Swagger 명세와 같다.
 *
 * <p>컨트롤러의 역할은 "주문 받는 직원"이다. 요청을 받아 서비스에 넘기고, 결과를 응답으로 돌려준다.
 * 규칙 검사(중복 등)나 DB 작업은 하지 않고 모두 IndexInfoService에 맡긴다.
 *
 * <ul>
 *   <li>{@code @RestController}: 메서드가 돌려준 객체를 JSON으로 바꿔서 응답한다.</li>
 *   <li>{@code @RequestMapping("/api/index-infos")}: 이 클래스의 모든 주소 앞에 붙는 공통 경로</li>
 *   <li>{@code @RequiredArgsConstructor}: final 필드(indexInfoService)를 받는 생성자를 Lombok이 만들고,
 *       스프링이 그 생성자로 서비스를 넣어 준다(의존성 주입).</li>
 *   <li>{@code @Tag}, {@code @Operation}: Swagger UI에 보이는 그룹 이름과 API 설명</li>
 * </ul>
 *
 * <p>에러 처리: 서비스에서 BusinessException이 던져지면 GlobalExceptionHandler가 알아서 응답한다.
 * 그래서 여기에는 try-catch가 없다.
 */
@Tag(name = "지수 정보 API", description = "지수 정보 관리 API")
@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  /**
   * 지수 정보 등록. POST /api/index-infos → 201 Created
   *
   * <ul>
   *   <li>{@code @RequestBody}: 요청 본문의 JSON을 IndexInfoCreateRequest로 바꿔서 받는다.</li>
   *   <li>{@code @Valid}: IndexInfoCreateRequest에 붙은 @NotBlank, @NotNull 등을 검사한다.
   *       틀리면 이 메서드는 실행되지 않고 400이 응답된다.</li>
   *   <li>ResponseEntity: 상태 코드를 직접 정할 때 쓴다. 기본은 200이라 등록은 201로 바꿔 준다.</li>
   * </ul>
   */
  @Operation(summary = "지수 정보 등록")
  @PostMapping
  public ResponseEntity<IndexInfoDto> create(@RequestBody @Valid IndexInfoCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(indexInfoService.create(request));
  }

  /**
   * 지수 정보 목록 조회. GET /api/index-infos?indexName=코스&size=10 → 200
   *
   * <p>{@code @ModelAttribute}: 주소 뒤의 쿼리 파라미터(?indexName=...&size=...)를
   * 같은 이름의 필드에 담아 IndexInfoSearchCondition 하나로 받는다.
   * 파라미터가 많아도 메서드가 깔끔해진다. 값이 없으면 기본값은 SearchCondition이 채운다.
   */
  @Operation(summary = "지수 정보 목록 조회")
  @GetMapping
  public CursorPageResponseDto<IndexInfoDto> search(@ModelAttribute IndexInfoSearchCondition condition) {
    return indexInfoService.search(condition);
  }

  /**
   * 지수 요약 목록. GET /api/index-infos/summaries → 200
   * 프론트엔드의 "지수 선택" 드롭다운에 쓰인다. 페이지 없이 전체를 준다.
   *
   * <p>"/summaries"와 아래 "/{id}"는 주소 모양이 비슷하지만,
   * 스프링은 글자가 정확히 맞는 "/summaries"를 먼저 고르기 때문에 충돌하지 않는다.
   */
  @Operation(summary = "지수 정보 요약 목록 조회")
  @GetMapping("/summaries")
  public List<IndexInfoSummaryDto> getSummaries() {
    return indexInfoService.getSummaries();
  }

  /**
   * 지수 정보 단건 조회. GET /api/index-infos/{id} → 200 (없으면 404)
   * {@code @PathVariable}: 주소의 {id} 자리 값을 꺼내 Long으로 받는다. (/api/index-infos/3 → id = 3)
   */
  @Operation(summary = "지수 정보 조회")
  @GetMapping("/{id}")
  public IndexInfoDto get(@PathVariable Long id) {
    return indexInfoService.get(id);
  }

  /**
   * 지수 정보 수정. PATCH /api/index-infos/{id} → 200
   * PATCH는 "보낸 값만 바꾸는" 수정이다. (전체를 바꾸는 PUT과 다르다)
   */
  @Operation(summary = "지수 정보 수정")
  @PatchMapping("/{id}")
  public IndexInfoDto update(@PathVariable Long id, @RequestBody @Valid IndexInfoUpdateRequest request) {
    return indexInfoService.update(id, request);
  }

  /**
   * 지수 정보 삭제. DELETE /api/index-infos/{id} → 204 No Content
   * 삭제는 돌려줄 내용이 없어서 본문 없이 204만 응답한다.
   * 그 지수의 지수 데이터와 자동 연동 설정도 함께 삭제된다.
   */
  @Operation(summary = "지수 정보 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexInfoService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
