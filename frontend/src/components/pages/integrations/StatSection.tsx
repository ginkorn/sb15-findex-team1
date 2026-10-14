import { useEffect } from "react";
import { useSyncJobListStore } from "@/store/syncJobStore";
import { formatDateSync } from "@/utils/date";
import { StatCard } from "./StatCard";

export const StatSection = () => {
  const { fetchStats, isLoadingStats, totalSuccess, totalFailed, latestSync } =
    useSyncJobListStore();

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const { datePart, timePart } = latestSync
    ? formatDateSync(latestSync)
    : { datePart: "연동 없음", timePart: "-" };

  const successValue = isLoadingStats
    ? "--"
    : totalSuccess.toLocaleString("ko-KR");
  const failedValue = isLoadingStats
    ? "--"
    : totalFailed.toLocaleString("ko-KR");

  return (
    <section className="scrollbar-thin flex shrink-0 gap-5 overflow-x-auto">
      <StatCard
        type="success"
        title="연동 성공"
        subtitle="최근 7일간"
        unit="건"
        value={successValue}
      />
      <StatCard
        type="error"
        title="연동 실패"
        subtitle="최근 7일간"
        unit="건"
        value={failedValue}
      />
      <StatCard
        type="pending"
        title="마지막 연동"
        subtitle={datePart}
        unit=""
        value={timePart}
      />
    </section>
  );
};
