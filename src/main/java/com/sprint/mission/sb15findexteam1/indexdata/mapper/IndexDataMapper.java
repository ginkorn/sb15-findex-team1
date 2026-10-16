
package com.sprint.mission.sb15findexteam1.indexdata.mapper;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataDto;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  // Entity의 indexInfoId를 DTO의 indexInfoId로 변환
  @Mapping(target = "indexInfoId", source = "indexInfoId")
  IndexDataDto toDto(IndexData indexData);

  // Entity 목록을 DTO 목록으로 변환
  List<IndexDataDto> toDtoList(List<IndexData> indexDataList);
}
