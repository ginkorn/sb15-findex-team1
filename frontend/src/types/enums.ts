export const SourceType = {
  OPEN_API: "OPEN_API",
  USER: "USER",
} as const;

export type SourceType = (typeof SourceType)[keyof typeof SourceType];

export const JobType = {
  INDEX_DATA: "INDEX_DATA",
  INDEX_INFO: "INDEX_INFO",
} as const;

export type JobType = (typeof JobType)[keyof typeof JobType];

export const ResultType = {
  SUCCESS: "SUCCESS",
  FAILED: "FAILED",
} as const;

export type ResultType = (typeof ResultType)[keyof typeof ResultType];
