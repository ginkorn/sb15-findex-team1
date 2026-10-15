package com.sprint.mission.sb15findexteam1.dashboard.mapper;

import com.sprint.mission.sb15findexteam1.dashboard.dto.ChartDataPointResponse;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// 빌드할 때 MapStruct가 이 인터페이스의 구현 클래스(ChartDataPointMapperImpl)를 자동으로 만든다.
// componentModel = "spring" : 구현체를 스프링 빈으로 등록해서 주입받을 수 있게 한다
@Mapper(componentModel = "spring")
public interface ChartDataPointMapper {

  // IndexData 한 건 → ChartDataPointResponse 한 건
  // 필드 이름이 서로 다르므로 "어떤 필드를 어디에 넣을지"를 알려 준다
  @Mapping(source = "baseDate", target = "date")           // 엔티티의 baseDate → DTO의 date
  @Mapping(source = "closingPrice", target = "value")      // 엔티티의 closingPrice → DTO의 value
  ChartDataPointResponse toPoint(IndexData data);

  // 목록 변환: 위 메서드를 각 원소에 적용해 리스트를 만든다 (MapStruct가 자동 구현)
  List<ChartDataPointResponse> toPoints(List<IndexData> rows);
}