import type { CursorPageResponse } from "@/model/pagination";
import type {
  AutoSyncConfigDto,
  AutoSyncConfigParams,
  SyncJobDto,
  SyncJobParams,
} from "@/model/sync";
import apiClient from "./client";

export const getSyncJobs = (params?: SyncJobParams) => {
  return apiClient.get<CursorPageResponse<SyncJobDto>>("/sync-jobs", params);
};

export const getAutoSyncConfigs = (params?: AutoSyncConfigParams) => {
  return apiClient.get<CursorPageResponse<AutoSyncConfigDto>>(
    "/auto-sync-configs",
    params,
  );
};

export const patchAutoSyncConfigs = (
  id: number,
  data: { enabled: boolean },
) => {
  return apiClient.patch(`/auto-sync-configs/${id}`, data);
};
