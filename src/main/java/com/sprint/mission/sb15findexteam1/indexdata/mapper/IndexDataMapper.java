
package com.sprint.mission.sb15findexteam1.indexdata.mapper;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  // Entity의 필드 값을 프론트엔드 응답 DTO에 전달
  IndexDataDto toDto(IndexData indexData);

  // 지수 데이터 목록 조회 API에서 사용
  List<IndexDataDto> toDtoList(List<IndexData> indexDataList);

  // TODO(A): IndexInfo 연관관계 변경 시
  // indexInfoId 매핑 방식 재확인
}
