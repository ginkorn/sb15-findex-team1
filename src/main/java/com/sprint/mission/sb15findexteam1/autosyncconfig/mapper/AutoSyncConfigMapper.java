package com.sprint.mission.sb15findexteam1.autosyncconfig.mapper;

import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigDto;
import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.AutoSyncConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * AutoSyncConfig 엔티티 → 응답 DTO 변환.
 * 지수 정보는 연관된 IndexInfo에서 꺼내므로, 이름이 다른 필드는 어디서 가져올지 직접 지정한다.
 */
@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  @Mapping(source = "indexInfo.indexClassification", target = "indexClassification")
  @Mapping(source = "indexInfo.indexName", target = "indexName")
  AutoSyncConfigDto toDto(AutoSyncConfig autoSyncConfig);
}
