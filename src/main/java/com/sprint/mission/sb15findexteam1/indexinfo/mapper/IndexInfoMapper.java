package com.sprint.mission.sb15findexteam1.indexinfo.mapper;

import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoDto;
import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import org.mapstruct.Mapper;

/**
 * IndexInfo 엔티티 → 응답 DTO "번역기".
 *
 * <p>인터페이스에 메서드 모양만 적어 두면, 구현 코드는 MapStruct가 빌드할 때 자동으로 만든다.
 * (build/generated/... 아래 IndexInfoMapperImpl.java가 생긴다)
 * 엔티티와 DTO의 필드 이름이 같으면 알아서 짝을 맞춰 값을 옮긴다.
 *
 * <p>{@code componentModel = "spring"}: 만들어진 구현체를 스프링 빈으로 등록한다.
 * 그래서 서비스에서 {@code private final IndexInfoMapper indexInfoMapper;}로 주입받아 쓸 수 있다.
 */
@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

  /** 지수 정보 → 전체 응답 (등록·조회·수정·목록용) */
  IndexInfoDto toDto(IndexInfo indexInfo);

  /** 지수 정보 → 요약 응답 (id, 분류명, 지수명만. 드롭다운용) */
  IndexInfoSummaryDto toSummaryDto(IndexInfo indexInfo);
}
