import type { ResultType } from "@/types/enums";

/**
 * 지수 데이터 DTO
 */
export interface IndexDataDto {
  id: number;
  indexInfoId: number;
  baseDate: string;
  sourceType: "USER" | "OPEN_API";
  marketPrice: number;
  closingPrice: number;
  highPrice: number;
  lowPrice: number;
  versus: number;
  fluctuationRate: number;
  tradingQuantity: number;
  tradingPrice: number;
  marketTotalAmount: number;
}

/**
 * 지수 데이터 생성 요청
 */
export interface IndexDataCreateRequest {
  indexInfoId: number;
  baseDate: string;
  marketPrice: number;
  closingPrice: number;
  highPrice: number;
  lowPrice: number;
  versus: number;
  fluctuationRate: number;
  tradingQuantity: number;
  tradingPrice: number;
  marketTotalAmount: number;
}

/**
 * 지수 데이터 수정 요청
 */
export interface IndexDataUpdateRequest {
  marketPrice?: number;
  closingPrice?: number;
  highPrice?: number;
  lowPrice?: number;
  versus?: number;
  fluctuationRate?: number;
  tradingQuantity?: number;
  tradingPrice?: number;
  marketTotalAmount?: number;
}

/**
 * 지수 데이터 조회 쿼리 파라미터
 */
export interface IndexDataListParams {
  indexInfoId?: number;
  startDate?: string;
  endDate?: string;
  idAfter?: number;
  cursor?: string;
  sortField?:
    | "baseDate"
    | "marketPrice"
    | "closingPrice"
    | "highPrice"
    | "lowPrice"
    | "versus"
    | "fluctuationRate"
    | "tradingQuantity"
    | "tradingPrice"
    | "marketTotalAmount";
  sortDirection?: "asc" | "desc";
  size?: number;
}

/**
 * 차트 데이터 포인트
 */
export interface ChartDataPoint {
  date: string;
  value: number;
}

/**
 * 차트 기간 유형
 */
export type PeriodType = "MONTHLY" | "QUARTERLY" | "YEARLY";

/**
 * 단위 기간 유형
 */
export type UnitPeriodType = "DAILY" | "WEEKLY" | "MONTHLY";

/**
 * 지수 차트 데이터 DTO
 */
export interface IndexChartDto {
  indexInfoId: number;
  indexClassification: string;
  indexName: string;
  periodType: PeriodType;
  dataPoints: ChartDataPoint[];
  ma5DataPoints: ChartDataPoint[];
  ma20DataPoints: ChartDataPoint[];
}

/**
 * 지수 성과 정보 DTO
 */
export interface IndexPerformanceDto {
  indexInfoId: number;
  indexClassification: string;
  indexName: string;
  versus: number;
  fluctuationRate: number;
  currentPrice: number;
  beforePrice: number;
}

/**
 * 순위가 포함된 지수 성과 정보 DTO
 */
export interface RankedIndexPerformanceDto {
  performance: IndexPerformanceDto;
  rank: number;
}

/**
 * CSV export 쿼리 파라미터
 */
export interface IndexDataExportParams {
  indexInfoId?: number;
  startDate?: string;
  endDate?: string;
  sortField?:
    | "baseDate"
    | "marketPrice"
    | "closingPrice"
    | "highPrice"
    | "lowPrice"
    | "versus"
    | "fluctuationRate"
    | "tradingQuantity"
    | "tradingPrice"
    | "marketTotalAmount";
  sortDirection?: "asc" | "desc";
}

/**
 * 지수 연동 DTO
 */
export interface IndexDataSyncDto {
  id: number;
  jobType: string;
  indexInfoId: number;
  targetDate: string;
  worker: string;
  jobTime: string;
  result: ResultType;
}

/**
 * 지수 데이터 연동 요청
 */
export interface IndexDataSyncRequest {
  indexInfoIds: number[];
  baseDateFrom: string;
  baseDateTo: string;
}
