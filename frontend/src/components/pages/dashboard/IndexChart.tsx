import { useEffect, useMemo, useRef, useState } from "react";
import { getIndexChart } from "@/api/dashboardApi";
import { Select } from "@/components/common/select/Select";
import { PeriodOptions, type PeriodKey } from "@/constants/dashboardOptions";
import type { IndexChartResponse } from "@/model/dashboard";
import type { Index } from "@/pages/DataManagement";
import { useIndexInfoSummaryStore } from "@/store/indexInfoSummaryStore";
import { IndexLineChart } from "./IndexLineChart";

interface IndexChartProps {
  onIndexChange?: (index: Index) => void;
}

const IndexChart = ({ onIndexChange }: IndexChartProps) => {
  const isInitializedRef = useRef(false);
  const {
    items,
    fetch: fetchSummaries,
    isLoading: isSummaryLoading,
  } = useIndexInfoSummaryStore();

  const [selectedPeriodId, setSelectedPeriodId] = useState<number>(
    PeriodOptions[0].id,
  );
  const [chartData, setChartData] = useState<IndexChartResponse | null>(null);
  const [isChartLoading, setIsChartLoading] = useState(false);
  const [indexInfoId, setIndexInfoId] = useState<number | null>(null);

  const summaries = useMemo(() => {
    if (!items || !Array.isArray(items)) {
      return [];
    }

    return items.map((item) => ({
      id: item.id,
      label: item.indexName,
    }));
  }, [items]);

  // 선택된 기간 키 도출
  const selectedPeriodValue = (PeriodOptions.find(
    (opt) => opt.id === selectedPeriodId,
  )?.value || "DAILY") as PeriodKey;

  // API 호출 로직
  const fetchChartData = async (id: number, period: PeriodKey) => {
    setIsChartLoading(true);
    try {
      const response = await getIndexChart(id, { periodType: period });
      const result = Array.isArray(response) ? response[0] : response;
      setChartData(result);
    } catch (error) {
      console.error("차트 데이터 로드 실패:", error);
      setChartData(null);
    } finally {
      setIsChartLoading(false);
    }
  };

  // 지수 목록 최초 조회
  useEffect(() => {
    void fetchSummaries();
  }, [fetchSummaries]);

  // 지수 목록 로드 완료 시 첫 번째 지수 자동 선택
  useEffect(() => {
    if (
      !isSummaryLoading &&
      summaries.length > 0 &&
      !isInitializedRef.current
    ) {
      const firstItem = summaries[0];
      setIndexInfoId(firstItem.id);
      onIndexChange?.(firstItem);
      isInitializedRef.current = true;
    }
  }, [isSummaryLoading, summaries, onIndexChange]);

  // 지수 ID나 기간이 바뀔 때마다 데이터 리로딩
  useEffect(() => {
    if (indexInfoId) {
      fetchChartData(indexInfoId, selectedPeriodValue);
    }
  }, [indexInfoId, selectedPeriodValue]);

  // 기간 선택 핸들러
  const handleUnitChange = (id: number) => {
    setSelectedPeriodId(id);
  };

  // 지수 선택 핸들러
  const handleSelectChange = (id: number) => {
    const selectedItem = summaries.find((item) => item.id === id);
    if (selectedItem) {
      setIndexInfoId(id);
      onIndexChange?.(selectedItem);
    }
  };

  return (
    <div className="border-secondary flex flex-col rounded-xl border bg-white shadow-xs">
      <div className="border-secondary flex w-full justify-between border-b px-6 py-5">
        <span className="text-lg font-semibold">지수 차트</span>
        <div className="flex gap-3">
          <Select
            items={PeriodOptions}
            aria-label="지수 차트 조회 단위 선택"
            defaultValue={PeriodOptions[0].id}
            onChange={(key) => handleUnitChange(Number(key))}
            className="w-28"
          >
            {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
          </Select>
          {!isSummaryLoading && indexInfoId !== null && (
            <Select
              items={summaries}
              aria-label="지수 선택"
              popoverClassName="scrollbar-thin"
              searchable
              defaultValue={indexInfoId}
              placeholder="지수 선택"
              onChange={(key) => handleSelectChange(key as number)}
              className="w-42"
            >
              {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
            </Select>
          )}
        </div>
      </div>
      <div className="p-6">
        {isChartLoading || isSummaryLoading ? (
          <p className="text-text-tertiary flex h-44 w-full items-center justify-center">
            데이터를 불러오는 중입니다...
          </p>
        ) : chartData &&
          chartData.dataPoints &&
          chartData.dataPoints.length > 0 ? (
          <IndexLineChart data={chartData} />
        ) : (
          <p className="text-text-tertiary flex h-72 w-full items-center justify-center text-sm font-semibold">
            데이터가 없습니다.
          </p>
        )}
      </div>
    </div>
  );
};

export default IndexChart;
