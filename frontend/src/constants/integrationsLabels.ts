import {
  JobType,
  ResultType,
  type JobType as JobTypeKey,
  type ResultType as ResultTypeKey,
} from "@/types/enums";

export const JobTypeLabels: Record<JobTypeKey, string> = {
  [JobType.INDEX_DATA]: "지수 데이터",
  [JobType.INDEX_INFO]: "지수 정보",
};

export const ResultTypeLabels: Record<ResultTypeKey, string> = {
  [ResultType.SUCCESS]: "성공",
  [ResultType.FAILED]: "실패",
};

export const IndexJobStatusOptions = [
  { id: 0, label: "전체 보기", value: "" },
  { id: 1, label: "활성화", value: true },
  { id: 2, label: "비활성화", value: false },
];

export const IndexSyncStatusOptions = [
  { id: 0, label: "전체 상태", value: "" },
  { id: 1, label: "성공", value: ResultType.SUCCESS },
  { id: 2, label: "실패", value: ResultType.FAILED },
];

export const IndexJobTypeOptions = [
  { id: 0, label: "전체 유형", value: "" },
  { id: 1, label: "지수 정보", value: JobType.INDEX_INFO },
  { id: 2, label: "지수 데이터", value: JobType.INDEX_DATA },
];
