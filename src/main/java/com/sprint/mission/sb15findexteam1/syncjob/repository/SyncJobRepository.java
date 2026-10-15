package com.sprint.mission.sb15findexteam1.syncjob.repository;

import com.sprint.mission.sb15findexteam1.syncjob.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {

}
