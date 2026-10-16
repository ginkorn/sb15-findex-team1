package com.sprint.mission.sb15findexteam1.indexinfo.service;

import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.sb15findexteam1.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import com.sprint.mission.sb15findexteam1.common.exception.BusinessException;
import com.sprint.mission.sb15findexteam1.common.exception.ErrorCode;
import com.sprint.mission.sb15findexteam1.indexdata.repository.IndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.mapper.IndexInfoMapper;
import com.sprint.mission.sb15findexteam1.indexinfo.repository.IndexInfoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지수 정보 서비스.
 *
 * <p>핵심 규칙
 * <ul>
 *   <li>(분류명 + 지수명) 중복 불가</li>
 *   <li>지수를 저장할 때는 항상 자동 연동 설정(꺼진 상태)도 함께 만든다 → {@link #register}에 한곳으로 모음</li>
 *   <li>지수를 지우면 그 지수의 지수 데이터도 함께 지운다</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용, 저장·수정·삭제 메서드만 @Transactional
public class IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;
  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexDataRepository indexDataRepository;
  private final IndexInfoMapper indexInfoMapper;

  /** 등록 (POST /api/index-infos) */
  @Transactional
  public IndexInfoDto create(IndexInfoCreateRequest request) {
    if (indexInfoRepository.existsByIndexClassificationAndIndexName(
        request.indexClassification(), request.indexName())) {
      throw new BusinessException(ErrorCode.DUPLICATE_INDEX_INFO,
          request.indexClassification() + " / " + request.indexName());
    }
    IndexInfo saved = register(request.indexClassification(), request.indexName(),
        request.employedItemsCount(), request.basePointInTime(), request.baseIndex(),
        SourceType.USER, Boolean.TRUE.equals(request.favorite()));
    return indexInfoMapper.toDto(saved);
  }

  /** 단건 조회 (GET /api/index-infos/{id}) */
  public IndexInfoDto get(Long id) {
    return indexInfoMapper.toDto(getEntity(id));
  }

  /**
   * 수정 (PATCH /api/index-infos/{id}). 보낸 값만 바뀐다.
   * 엔티티 값만 바꾸면 트랜잭션이 끝날 때 JPA가 UPDATE를 보낸다(변경 감지).
   */
  @Transactional
  public IndexInfoDto update(Long id, IndexInfoUpdateRequest request) {
    IndexInfo indexInfo = getEntity(id);
    indexInfo.update(request.employedItemsCount(), request.basePointInTime(),
        request.baseIndex(), request.favorite());
    return indexInfoMapper.toDto(indexInfo);
  }

  /**
   * 삭제 (DELETE /api/index-infos/{id}).
   * <ul>
   *   <li>지수 데이터: indexInfoId만 가지고 있어 DB가 자동으로 지우지 않으므로 여기서 직접 삭제</li>
   *   <li>자동 연동 설정: DB가 함께 삭제 (ON DELETE CASCADE)</li>
   *   <li>연동 이력: 지수 칸만 비우고 기록은 남김 (ON DELETE SET NULL)</li>
   * </ul>
   */
  @Transactional
  public void delete(Long id) {
    IndexInfo indexInfo = getEntity(id);
    indexDataRepository.deleteAllByIndexInfoIdInBulk(id);
    indexInfoRepository.delete(indexInfo);
  }

  /** 목록 조회 (GET /api/index-infos): 필터 + 정렬 + 커서 페이지네이션 */
  public CursorPageResponseDto<IndexInfoDto> search(IndexInfoSearchCondition condition) {
    List<IndexInfoDto> rows = indexInfoRepository.findPageByCondition(condition).stream()
        .map(indexInfoMapper::toDto)
        .toList();
    long total = indexInfoRepository.countByCondition(condition);
    return CursorPageResponseDto.of(rows, condition.size(), total,
        dto -> cursorValue(dto, condition.sortField()), IndexInfoDto::id);
  }

  /** 요약 목록 (GET /api/index-infos/summaries): 드롭다운용 전체 목록, 분류명 → 지수명 순 */
  public List<IndexInfoSummaryDto> getSummaries() {
    return indexInfoRepository.findAll(Sort.by("indexClassification", "indexName")).stream()
        .map(indexInfoMapper::toSummaryDto)
        .toList();
  }

  /**
   * Open API 연동용 등록·수정 (C가 호출).
   *
   * <p>(분류명 + 지수명)으로 찾아서
   * <ul>
   *   <li>없으면: OPEN_API 출처로 새로 등록 + 자동 연동 설정(꺼짐) 생성</li>
   *   <li>있으면: 채용 종목 수, 기준 시점, 기준 지수만 최신 값으로 수정 (즐겨찾기는 사용자 값 유지)</li>
   * </ul>
   * Open API의 문자열(예: basPntm "19800104")은 C가 LocalDate·BigDecimal로 바꿔서 넘긴다.
   *
   * @return 저장된 지수 엔티티 (연동 이력·지수 데이터 저장에 id를 쓰기 위해 엔티티로 반환)
   */
  @Transactional
  public IndexInfo upsertFromOpenApi(String indexClassification, String indexName,
      Integer employedItemsCount, LocalDate basePointInTime, BigDecimal baseIndex) {
    return indexInfoRepository.findByIndexClassificationAndIndexName(indexClassification, indexName)
        .map(existing -> {
          existing.update(employedItemsCount, basePointInTime, baseIndex, null);
          return existing;
        })
        .orElseGet(() -> register(indexClassification, indexName, employedItemsCount,
            basePointInTime, baseIndex, SourceType.OPEN_API, false));
  }

  /** B·C용: id로 지수 엔티티 조회, 없으면 404 */
  public IndexInfo getEntity(Long id) {
    return indexInfoRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.INDEX_INFO_NOT_FOUND, "id=" + id));
  }

  /** 지수 저장 + 자동 연동 설정 생성을 한곳에서. 사용자 등록과 Open API 등록이 모두 여기를 거친다. */
  private IndexInfo register(String indexClassification, String indexName, Integer employedItemsCount,
      LocalDate basePointInTime, BigDecimal baseIndex, SourceType sourceType, boolean favorite) {
    IndexInfo saved = indexInfoRepository.save(IndexInfo.create(indexClassification, indexName,
        employedItemsCount, basePointInTime, baseIndex, sourceType, favorite));
    autoSyncConfigRepository.save(AutoSyncConfig.createDisabled(saved));
    return saved;
  }

  /** 다음 페이지 cursor로 쓸 값 = 마지막 항목의 정렬 필드 값 */
  private Object cursorValue(IndexInfoDto dto, String sortField) {
    return switch (sortField) {
      case IndexInfoSearchCondition.SORT_NAME -> dto.indexName();
      case IndexInfoSearchCondition.SORT_EMPLOYED_ITEMS_COUNT -> dto.employedItemsCount();
      default -> dto.indexClassification();
    };
  }
}
