package com.sprint.mission.sb15findexteam1.indexdata.service;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.mapper.IndexDataMapper;
import com.sprint.mission.sb15findexteam1.indexdata.repository.IndexDataRepository;
import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 지수 데이터의 등록, 조회, 수정, 삭제를 처리하는 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataService {

  // DB에 접근하는 Repository
  private final IndexDataRepository indexDataRepository;

  // Entity를 응답 DTO로 변환하는 Mapper
  private final IndexDataMapper indexDataMapper;


  // 사용자가 입력한 지수 데이터를 DB에 등록
  @Transactional
  public IndexDataDto create(IndexDataCreateRequest request) {

    // 동일한 지수 ID와 기준 날짜의 데이터가 있는지 검사
    boolean exists = indexDataRepository.existsByIndexInfoIdAndBaseDate(
        request.indexInfoId(),
        request.baseDate()
    );

    // 이미 존재한다면 중복 등록 방지
    if (exists) {
      throw new IllegalArgumentException(
          "이미 등록된 지수 데이터입니다."
      );
    }

    // TODO(A): IndexInfoRepository 구현 후
    // indexInfoId에 해당하는 지수가 실제로 존재하는지 검증

    // DTO의 데이터를 Entity로 변환
    IndexData indexData = IndexData.builder()
        .indexInfoId(request.indexInfoId())
        .baseDate(request.baseDate())
        .sourceType("USER")
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


  // 검색 조건에 맞는 지수 데이터 목록 조회
  public List<IndexDataDto> findAll(
      IndexDataSearchCondition condition
  ) {

    // QueryDSL로 필터, 정렬, 커서를 적용하여 조회
    // 다음 페이지 확인을 위해 size + 1개가 반환됨
    List<IndexData> indexDataList =
        indexDataRepository.findPageByCondition(condition);

    // 조회 결과를 DTO 목록으로 변환
    // TODO(A): 공통 CursorPageResponse 확정 후
    // hasNext, nextCursor, nextIdAfter 계산 및 응답 연결
    return indexDataMapper.toDtoList(indexDataList);
  }


  // 검색 조건에 해당하는 전체 데이터 개수 조회
  public long count(IndexDataSearchCondition condition) {

    // 커서와 관계없이 필터 조건에 해당하는 전체 개수 반환
    return indexDataRepository.countByCondition(condition);
  }


  // 특정 ID의 지수 데이터 삭제
  @Transactional
  public void delete(Long id) {

    // 삭제할 데이터가 DB에 존재하는지 조회
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "존재하지 않는 지수 데이터입니다. ID: " + id
        ));

    // 조회한 Entity를 DB에서 삭제
    indexDataRepository.delete(indexData);
  }


  // TODO(B): IndexData Entity에 수정 메서드를 추가한 뒤
  // IndexDataUpdateRequest를 사용하는 PATCH 기능 구현

  // TODO(C): 외부 Open API 연동용 데이터 Upsert 기능 구현

  // TODO(A): IndexInfo 연관관계 및 SourceType Enum 적용

  // TODO(A): 예외 응답의 HTTP 상태 코드 정책 확인

  // 기존 지수 데이터 수정
  @Transactional
  public IndexDataDto update(Long id, IndexDataUpdateRequest request) {

    // 수정할 데이터를 ID로 조회
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "존재하지 않는 지수 데이터입니다. ID: " + id
        ));

    // 요청받은 필드만 변경
    // null인 필드는 Entity의 update()에서 변경하지 않음
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

    // 변경된 Entity를 응답 DTO로 변환하여 반환
    // @Transactional의 변경 감지 기능으로 DB에 반영됨
    return indexDataMapper.toDto(indexData);
  }

}
