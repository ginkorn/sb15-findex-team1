package com.sprint.mission.sb15findexteam1.autosyncconfig.repository;

import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.AutoSyncConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** 자동 연동 설정 저장소 */
public interface AutoSyncConfigRepository
    extends JpaRepository<AutoSyncConfig, Long>, AutoSyncConfigQueryRepository {

  /** C의 배치가 쓰는 조회: 자동 연동이 켜진 지수의 id 목록 */
  @Query("select c.indexInfo.id from AutoSyncConfig c where c.enabled = true order by c.indexInfo.id")
  List<Long> findEnabledIndexInfoIds();
}
