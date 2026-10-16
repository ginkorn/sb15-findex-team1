package com.sprint.mission.sb15findexteam1.indexdata.service;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.mapper.IndexDataMapper;
import com.sprint.mission.sb15findexteam1.indexdata.repository.IndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import com.sprint.mission.sb15findexteam1.indexinfo.repository.IndexInfoRepository;
import com.sprint.mission.sb15findexteam1.common.exception.BusinessException;
import com.sprint.mission.sb15findexteam1.common.exception.ErrorCode;
import java.util.Optional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataService {

  // DB에 접근하는 Repository
  private final IndexDataRepository indexDataRepository;

  // Entity를 응답 DTO로 변환하는 Mapper
  private final IndexDataMapper indexDataMapper;

  // 지수 정보가 실제로 존재하는지 확인하기 위한 Repository
  private final IndexInfoRepository indexInfoRepository;

  @Transactional
  public IndexDataDto create(IndexDataCreateRequest request) {

    // 등록하려는 지수 정보가 실제로 존재하는지 검증
    if (!indexInfoRepository.existsById(request.indexInfoId())) {
      throw new BusinessException(
          ErrorCode.INDEX_INFO_NOT_FOUND,
          "indexInfoId=" + request.indexInfoId()
      );
    }

    // 동일한 지수 ID와 기준 날짜의 데이터가 있는지 검사
    boolean exists = indexDataRepository.existsByIndexInfoIdAndBaseDate(
        request.indexInfoId(),
        request.baseDate()
    );

    // 중복된 데이터는 등록하지 않음
    if (exists) {
      throw new BusinessException(
          ErrorCode.DUPLICATE_INDEX_DATA,
          "indexInfoId=" + request.indexInfoId()
              + ", baseDate=" + request.baseDate()
      );
    }

    IndexData indexData = IndexData.builder()
        .indexInfoId(request.indexInfoId())
        .baseDate(request.baseDate())
        .sourceType(SourceType.USER)
        .marketPrice(request.marketPrice())
        .closingPrice(request.closingPrice())
        .highPrice(request.highPrice())
        .lowPrice(request.lowPrice())
        .versus(request.versus())
        .fluctuationRate(request.fluctuationRate())
        .tradingQuantity(request.tradingQuantity())
        .tradingPrice(request.tradingPrice())
        .marketTotalAmount(request.marketTotalAmount())
        .build();

    // Repository를 이용해 DB에 저장
    IndexData saved = indexDataRepository.save(indexData);

    // 저장된 Entity를 응답 DTO로 변환하여 반환
    return indexDataMapper.toDto(saved);
  }

  @Transactional
  public IndexDataDto upsertFromOpenApi(IndexDataCreateRequest request) {

    // 지수 정보가 실제로 존재하는지 확인
    if (!indexInfoRepository.existsById(request.indexInfoId())) {
      throw new BusinessException(
          ErrorCode.INDEX_INFO_NOT_FOUND,
          "indexInfoId=" + request.indexInfoId()
      );
    }

    Optional<IndexData> existing =
        indexDataRepository.findByIndexInfoIdAndBaseDate(
            request.indexInfoId(),
            request.baseDate()
        );

    if (existing.isPresent()) {
      IndexData indexData = existing.get();

      indexData.update(
          request.marketPrice(),
          request.closingPrice(),
          request.highPrice(),
          request.lowPrice(),
          request.versus(),
          request.fluctuationRate(),
          request.tradingQuantity(),
          request.tradingPrice(),
          request.marketTotalAmount()
      );

      return indexDataMapper.toDto(indexData);
    }

    IndexData indexData = IndexData.builder()
        .indexInfoId(request.indexInfoId())
        .baseDate(request.baseDate())
        .sourceType(SourceType.OPEN_API)
        .marketPrice(request.marketPrice())
        .closingPrice(request.closingPrice())
        .highPrice(request.highPrice())
        .lowPrice(request.lowPrice())
        .versus(request.versus())
        .fluctuationRate(request.fluctuationRate())
        .tradingQuantity(request.tradingQuantity())
        .tradingPrice(request.tradingPrice())
        .marketTotalAmount(request.marketTotalAmount())
        .build();

    IndexData saved = indexDataRepository.save(indexData);

    return indexDataMapper.toDto(saved);
  }

  public CursorPageResponseDto<IndexDataDto> findAll(
      IndexDataSearchCondition condition
  ) {

    int size = CursorPaging.normalizeSize(condition.size());

    IndexDataSearchCondition pagingCondition =
        new IndexDataSearchCondition(
            condition.indexInfoId(),
            condition.startDate(),
            condition.endDate(),
            condition.idAfter(),
            condition.cursor(),
            condition.sortField(),
            condition.sortDirection(),
            size
        );

    List<IndexDataDto> rows = indexDataMapper.toDtoList(
        indexDataRepository.findPageByCondition(pagingCondition)
    );

    // 검색 조건에 일치하는 전체 데이터 개수
    long total = indexDataRepository.countByCondition(pagingCondition);

    // 기본 정렬 기준은 baseDate
    String sortField = condition.sortField() == null
        || condition.sortField().isBlank()
        ? "baseDate"
        : condition.sortField();

    // 공통 DTO에서 데이터 수, 다음 커서, 다음 페이지 여부 계산
    return CursorPageResponseDto.of(
        rows,
        size,
        total,
        dto -> cursorValue(dto, sortField),
        IndexDataDto::id
    );
  }

  // 검색 조건에 해당하는 전체 데이터 개수 조회
  public long count(IndexDataSearchCondition condition) {

    // 커서와 관계없이 필터 조건에 해당하는 전체 개수 반환
    return indexDataRepository.countByCondition(condition);
  }

  @Transactional
  public void delete(Long id) {

    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new BusinessException(
            ErrorCode.INDEX_DATA_NOT_FOUND,
            "id=" + id
        ));

    indexDataRepository.delete(indexData);
  }

  @Transactional
  public IndexDataDto update(Long id, IndexDataUpdateRequest request) {

    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new BusinessException(
            ErrorCode.INDEX_DATA_NOT_FOUND,
            "id=" + id
        ));

    indexData.update(
        request.marketPrice(),
        request.closingPrice(),
        request.highPrice(),
        request.lowPrice(),
        request.versus(),
        request.fluctuationRate(),
        request.tradingQuantity(),
        request.tradingPrice(),
        request.marketTotalAmount()
    );

    return indexDataMapper.toDto(indexData);
  }

  // 정렬 기준에 따라 다음 페이지 커서 값을 추출
  private Object cursorValue(IndexDataDto dto, String sortField) {
    return switch (sortField) {
      case "baseDate" -> dto.baseDate();
      case "marketPrice" -> dto.marketPrice();
      case "closingPrice" -> dto.closingPrice();
      case "highPrice" -> dto.highPrice();
      case "lowPrice" -> dto.lowPrice();
      case "versus" -> dto.versus();
      case "fluctuationRate" -> dto.fluctuationRate();
      case "tradingQuantity" -> dto.tradingQuantity();
      case "tradingPrice" -> dto.tradingPrice();
      case "marketTotalAmount" -> dto.marketTotalAmount();

      default -> throw new IllegalArgumentException(
          "지원하지 않는 정렬 필드: " + sortField
      );
    };
  }
}
