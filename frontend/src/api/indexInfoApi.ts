import type {
  IndexInfoListParams,
  IndexInfoRequest,
  IndexInfoResponse,
  IndexInfoSummary,
  SyncIndexResponse,
} from "@/model/indexInfo";
import type { CursorPageResponse } from "@/model/pagination";
import apiClient from "./client";

// 지수 정보 목록 조회
export const getIndexInfoList = (params?: IndexInfoListParams) => {
  return apiClient.get<CursorPageResponse<IndexInfoResponse>>(
    "/index-infos",
    params,
  );
};

// 지수 정보 상세 조회
export const getIndexInfo = (id: number) => {
  return apiClient.get<IndexInfoResponse>(`/index-infos/${id}`);
};

// 지수 정보 등록
export const createIndexInfo = (data: IndexInfoRequest) => {
  return apiClient.post<IndexInfoResponse>("/index-infos", data);
};

// 지수 정보 수정
export const updateIndexInfo = (id: number, data: IndexInfoRequest) => {
  return apiClient.patch<IndexInfoResponse>(`/index-infos/${id}`, data);
};

// 지수 정보 요약 목록 조회
export const getIndexInfoSummaries = () => {
  return apiClient.get<IndexInfoSummary[]>("/index-infos/summaries");
};

// 지수 정보 삭제
export const deleteIndexInfo = (id: number) => {
  return apiClient.delete<IndexInfoResponse>(`/index-infos/${id}`);
};

// 지수 정보 연동
export const syncIndexInfo = async () => {
  const data = await apiClient.post<SyncIndexResponse[]>(
    `/sync-jobs/index-infos`,
  );
  // 성공, 실패 개수 계산
  const result = data.reduce(
    (
      acc: { successCount: number; failCount: number },
      info: SyncIndexResponse,
    ) => {
      if (info.result === "SUCCESS") {
        acc.successCount += 1;
      } else {
        acc.failCount += 1;
      }
      return acc;
    },
    { successCount: 0, failCount: 0 },
  );

  return result;
};
