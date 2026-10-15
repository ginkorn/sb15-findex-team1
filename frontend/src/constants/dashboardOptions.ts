// 일/주/월간 조회 단위
export const DateUnitOptions = [
  { id: 1, label: "일간", value: "DAILY" },
  { id: 2, label: "주간", value: "WEEKLY" },
  { id: 3, label: "월간", value: "MONTHLY" },
];

export type DateUnitKey = "DAILY" | "WEEKLY" | "MONTHLY";

// 기간별 조회 범위
export const PeriodOptions = [
  { id: 1, label: "1달", value: "MONTHLY" },
  { id: 2, label: "3달", value: "QUARTERLY" },
  { id: 3, label: "1년", value: "YEARLY" },
];

export type PeriodKey = "MONTHLY" | "QUARTERLY" | "YEARLY";
