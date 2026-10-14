import { SourceType, type SourceType as SourceTypeKey } from "@/types/enums";

export const SourceTypeLabels: Record<SourceTypeKey, string> = {
  [SourceType.OPEN_API]: "Open API",
  [SourceType.USER]: "사용자",
};
