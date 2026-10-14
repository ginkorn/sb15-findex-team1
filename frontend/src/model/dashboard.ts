// 주요 지수
export interface MajorIndexResponse {
  indexInfoId: number;
  indexClassification: string;
  indexName: string;
  versus: number;
  fluctuationRate: number;
  currentPrice: number;
  beforePrice: number;
  rank?: number;
}

// 지수 차트
export interface IndexChartResponse {
  indexInfoId: number;
  indexClassification: string;
  indexName: string;
  periodType: string;
  dataPoints: {
    date: string;
    value: number;
  }[];
  ma5DataPoints: {
    date: string;
    value: number;
  }[];
  ma20DataPoints: {
    date: string;
    value: number;
  }[];
}

// 지수 요약
export interface IndexSummaryResponse {
  id: number;
  indexClassification: string;
  indexName: string;
}

// 지수 성과
export interface IndexPerformanceResponse {
  rank: number;
  performance: MajorIndexResponse;
}
