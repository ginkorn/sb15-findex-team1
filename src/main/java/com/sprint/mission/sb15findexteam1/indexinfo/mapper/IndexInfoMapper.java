package com.sprint.mission.sb15findexteam1.indexinfo.mapper;

import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import org.mapstruct.Mapper;

/**
 * IndexInfo 엔티티 → 응답 DTO 변환. 구현 코드는 MapStruct가 빌드할 때 자동으로 만든다.
 * 필드 이름이 같으면 알아서 연결된다.
 */
@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

  IndexInfoDto toDto(IndexInfo indexInfo);

  IndexInfoSummaryDto toSummaryDto(IndexInfo indexInfo);
}
