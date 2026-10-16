package com.sprint.mission.sb15findexteam1.indexinfo.repository;

import com.sprint.mission.sb15findexteam1.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import java.util.List;

/** QueryDSL로 구현하는 지수 정보 목록 검색 */
public interface IndexInfoQueryRepository {

  /** 필터·정렬·커서를 적용해 size + 1개를 조회한다. (+1은 다음 페이지 확인용) */
  List<IndexInfo> findPageByCondition(IndexInfoSearchCondition condition);

  /** 커서를 뺀 필터 조건만으로 전체 개수를 센다. */
  long countByCondition(IndexInfoSearchCondition condition);
}
