package com.sprint.mission.sb15findexteam1.autosyncconfig.repository;

import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigSearchCondition;
import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.AutoSyncConfig;
import java.util.List;

/** QueryDSL로 구현하는 자동 연동 설정 목록 검색 */
public interface AutoSyncConfigQueryRepository {

  /** 필터·정렬·커서를 적용해 size + 1개를 조회한다. 지수 정보도 함께 가져온다(fetch join). */
  List<AutoSyncConfig> findPageByCondition(AutoSyncConfigSearchCondition condition);

  /** 커서를 뺀 필터 조건만으로 전체 개수를 센다. */
  long countByCondition(AutoSyncConfigSearchCondition condition);
}
