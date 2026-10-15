import type {
  BadgeColors,
  BadgeTypes,
} from "@/components/common/badges/badge-types";
import { SourceTypeLabels } from "@/constants/indexCommonLabels";
import {
  JobTypeLabels,
  ResultTypeLabels,
} from "@/constants/integrationsLabels";
import {
  JobType,
  ResultType,
  SourceType,
  type JobType as JobTypeKey,
  type ResultType as ResultTypeKey,
  type SourceType as SourceTypeKey,
} from "@/types/enums";
import { BadgeWithDot, Badge as UI_Badge } from "./Badges";

// Props로 받을 종류
export type BadgeKind = "source" | "job" | "result";
export type BadgeValue = SourceType | JobType | ResultType;
type BadgeConfig = {
  label: string;
  type: BadgeTypes;
  color: BadgeColors;
  withDot?: boolean;
};

// 소스타입
const sourceConfig: Record<string, BadgeConfig> = {
  [SourceType.USER]: {
    label: SourceTypeLabels[SourceType.USER],
    type: "pill-color",
    color: "purple",
  },
  [SourceType.OPEN_API]: {
    label: SourceTypeLabels[SourceType.OPEN_API],
    type: "pill-color",
    color: "blue",
  },
};

// 유형
const jobConfig: Record<JobTypeKey, BadgeConfig> = {
  [JobType.INDEX_DATA]: {
    label: JobTypeLabels[JobType.INDEX_DATA],
    type: "color",
    color: "pink",
  },
  [JobType.INDEX_INFO]: {
    label: JobTypeLabels[JobType.INDEX_INFO],
    type: "color",
    color: "gray",
  },
};

// 처리 결과
const resultConfig: Record<ResultTypeKey, BadgeConfig> = {
  [ResultType.SUCCESS]: {
    label: ResultTypeLabels[ResultType.SUCCESS],
    type: "pill-color",
    color: "success",
    withDot: true,
  },
  [ResultType.FAILED]: {
    label: ResultTypeLabels[ResultType.FAILED],
    type: "pill-color",
    color: "error",
    withDot: true,
  },
};

interface BadgeProps {
  kind: BadgeKind;
  value: string;
  className?: string;
}

export const Badge = ({ kind, value, className }: BadgeProps) => {
  let config: BadgeConfig | undefined;

  if (kind === "source") {
    config = sourceConfig[value as SourceTypeKey];
  } else if (kind === "job") {
    config = jobConfig[value as JobTypeKey];
  } else if (kind === "result") {
    config = resultConfig[value as ResultTypeKey];
  }

  if (!config) return null;

  const { type, color, label, withDot } = config;

  return withDot ? (
    <BadgeWithDot type={type} color={color} className={className}>
      {label}
    </BadgeWithDot>
  ) : (
    <UI_Badge type={type} color={color} className={className}>
      {label}
    </UI_Badge>
  );
};
