import type {
  IndexChartDto,
  IndexDataCreateRequest,
  IndexDataDto,
  IndexDataExportParams,
  IndexDataListParams,
  IndexDataSyncDto,
  IndexDataSyncRequest,
  IndexDataUpdateRequest,
  IndexPerformanceDto,
  PeriodType,
  RankedIndexPerformanceDto,
  UnitPeriodType,
} from "@/model/indexData";
import type { CursorPageResponse } from "@/model/pagination";
import apiClient from "./client";

/**
 * 지수 데이터 목록 조회
 */
export const getIndexDataList = (params?: IndexDataListParams) => {
  return apiClient.get<CursorPageResponse<IndexDataDto>>("/index-data", params);
};

/**
 * 지수 데이터 등록
 */
export const createIndexData = (data: IndexDataCreateRequest) => {
  return apiClient.post<IndexDataDto>("/index-data", data);
};

/**
 * 지수 데이터 수정
 */
export const updateIndexData = (id: number, data: IndexDataUpdateRequest) => {
  return apiClient.patch<IndexDataDto>(`/index-data/${id}`, data);
};

/**
 * 지수 데이터 삭제
 */
export const deleteIndexData = (id: number) => {
  return apiClient.delete<void>(`/index-data/${id}`);
};

/**
 * 지수 차트 조회
 */
export const getIndexChart = (
  id: number,
  periodType: PeriodType = "MONTHLY",
) => {
  return apiClient.get<IndexChartDto>(`/index-data/${id}/chart`, {
    periodType,
  });
};

/**
 * 지수 성과 랭킹 조회
 */
export const getIndexPerformanceRank = (params?: {
  indexInfoId?: number;
  periodType?: UnitPeriodType;
  limit?: number;
}) => {
  return apiClient.get<RankedIndexPerformanceDto[]>(
    "/index-data/performance/rank",
    params,
  );
};

/**
 * 관심 지수 성과 조회
 */
export const getFavoriteIndexPerformance = (
  periodType: UnitPeriodType = "DAILY",
) => {
  return apiClient.get<IndexPerformanceDto[]>(
    "/index-data/performance/favorite",
    { periodType },
  );
};

/**
 * 지수 데이터 CSV export
 */
export const downloadIndexDataCSV = (params?: IndexDataExportParams) => {
  return apiClient.get<Blob>("/index-data/export/csv", params);
};

/**
 * 지수 데이터 연동
 */
export const syncIndexData = (data: IndexDataSyncRequest) => {
  return apiClient.post<IndexDataSyncDto[]>("/sync-jobs/index-data", data);
};
