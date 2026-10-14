import type { JobType, ResultType } from "@/types/enums";

/**
 * 연동 작업 DTO
 */
export interface SyncJobDto {
  id: number;
  jobType: JobType;
  indexInfoId: number;
  targetDate: string;
  worker: string;
  jobTime: string;
  result: ResultType;
}

/**
 * 연동 작업 조회 쿼리 파라미터
 */
export interface SyncJobParams {
  jobType?: JobType;
  indexInfoId?: number;
  baseDateFrom?: string;
  baseDateTo?: string;
  worker?: string;
  jobTimeFrom?: string;
  jobTimeTo?: string;
  status?: ResultType;
  idAfter?: number;
  cursor?: string;
  sortField?: "targetDate" | "jobTime";
  sortDirection?: "asc" | "desc";
  size?: number;
}

/**
 * 자동 연동 설정 DTO
 */
export interface AutoSyncConfigDto {
  id: number;
  indexInfoId: number;
  indexClassification: string;
  indexName: string;
  enabled: boolean;
}

/**
 * 자동 연동 설정 조회 쿼리 파라미터
 */
export interface AutoSyncConfigParams {
  indexInfoId?: number;
  enabled?: boolean;
  idAfter?: number;
  cursor?: string;
  sortField?: "indexInfo" | "indexName";
  sortDirection?: "asc" | "desc";
  size?: number;
}
