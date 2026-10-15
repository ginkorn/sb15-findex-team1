import type { SourceType } from "@/types/enums";

// 지수 정보 목록 조회
export interface IndexInfoListParams {
  indexClassification?: string;
  indexName?: string;
  favorite?: boolean;
  idAfter?: number;
  cursor?: string;
  sortField?: "indexClassification" | "indexName" | "employedItemsCount";
  sortDirection?: "asc" | "desc";
  size?: number;
}

// 지수 정보 상세 조회
export interface IndexInfoResponse {
  id: number;
  indexClassification?: string;
  indexName?: string;
  employedItemsCount: number;
  // "YYYY-MM-DD"
  basePointInTime: string;
  baseIndex: number;
  sourceType: SourceType;
  favorite: boolean;
}

// 지수 정보 등록, 수정
export interface IndexInfoRequest {
  indexClassification?: string;
  indexName?: string;
  employedItemsCount: number;
  // "YYYY-MM-DD"
  basePointInTime: string;
  baseIndex: number;
  favorite: boolean;
}

// 지수 정보 요약
export interface IndexInfoSummary {
  id: number;
  indexClassification: string;
  indexName: string;
}

// 지수 연동
export interface SyncIndexResponse {
  id: number;
  jobType: string;
  indexInfoId: number;
  targetDate: string | null;
  worker: string;
  jobTime: string;
  result: "SUCCESS" | "FAIL";
}
