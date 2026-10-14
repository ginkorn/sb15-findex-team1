import type { DateUnitKey, PeriodKey } from "@/constants/dashboardOptions";
import type {
  IndexChartResponse,
  IndexPerformanceResponse,
  MajorIndexResponse,
} from "@/model/dashboard";
import type { IndexInfoSummary } from "@/model/indexInfo";
import apiClient from "./client";

// 주요 지수 조회
export const getMajorIndexPerformance = (params: {
  periodType: DateUnitKey;
}) => {
  return apiClient.get<MajorIndexResponse[]>(
    "/index-data/performance/favorite",
    params,
  );
};

// 지수 차트 조회
export const getIndexChart = (
  id: number,
  params: { periodType: PeriodKey },
) => {
  return apiClient.get<IndexChartResponse[]>(`/index-data/${id}/chart`, params);
};

// 지수 정보 요약 조회
export const getIndexInfoSummaries = () => {
  return apiClient.get<IndexInfoSummary[]>("/index-infos/summaries");
};

// 지수 성과 조회
export const getIndexPerformanceRank = (params: {
  indexInfoId?: number;
  periodType: DateUnitKey;
  limit?: number;
}) => {
  return apiClient.get<IndexPerformanceResponse[]>(
    "/index-data/performance/rank",
    params,
  );
};
