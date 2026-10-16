package com.sprint.mission.sb15findexteam1.syncjob.repository;

import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 연동 작업 이력(SyncJob) 저장소. 이력 목록 조회용 쿼리는 D가 추가한다. */
@Repository
public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {
}
