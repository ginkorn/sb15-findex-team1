package com.sprint.mission.sb15findexteam1.syncjob.scheduler;

import com.sprint.mission.sb15findexteam1.autosyncconfig.service.AutoSyncConfigService;
import com.sprint.mission.sb15findexteam1.syncjob.service.SyncJobService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoSyncScheduler {
  private static final String SYSTEM_WORKER = "system";

  private final AutoSyncConfigService autoSyncConfigService;
  private final SyncJobService syncJobService;

  @Scheduled(cron = "0 0 14 * * MON-FRI", zone = "Asia/Seoul")
  public void syncEnabledIndexes() {
    List<Long> indexInfoIds = autoSyncConfigService.findEnabledIndexInfoIds();
    if (indexInfoIds.isEmpty()) {
      return;
    }

    syncJobService.syncLatestIndexData(
        indexInfoIds,
        SYSTEM_WORKER
    );

  }

}
