package com.sprint.mission.sb15findexteam1.syncjob.entity;

import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
import com.sprint.mission.sb15findexteam1.syncjob.repository.SyncJobRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SyncJobTest {

  @Autowired
  SyncJobRepository syncJobRepository;

  @Test
  public void 엔티티_저장_및_꺼내오기_테스트() {
    SyncJob syncJob = SyncJob.create(
        JobType.INDEX_DATA,
        null,
        LocalDate.now(),
        "worker",
        ResultType.SUCCESS
    );

    syncJobRepository.save(syncJob);

    syncJob = syncJobRepository.findById(syncJob.getId()).orElseThrow();

    System.out.println(syncJob.getTargetDate());
  }
}