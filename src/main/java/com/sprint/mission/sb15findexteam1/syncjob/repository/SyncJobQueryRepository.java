package com.sprint.mission.sb15findexteam1.syncjob.repository;

import com.sprint.mission.sb15findexteam1.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import java.util.List;

public interface SyncJobQueryRepository {

  // 조건에 맞는 이력을 정렬해서 size + 1개까지 조회 (다음 페이지가 있는지 알기 위해 1개 더)
  List<SyncJob> findPageByCondition(SyncJobSearchCondition condition);

  // 커서를 뺀 필터 조건에 맞는 전체 개수
  long countByCondition(SyncJobSearchCondition condition);

}
