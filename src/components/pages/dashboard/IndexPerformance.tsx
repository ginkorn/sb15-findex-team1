import { useEffect, useMemo, useState } from "react";
import { getIndexPerformanceRank } from "@/api/indexDataApi";
import { Select } from "@/components/common/select/Select";
import { Table } from "@/components/common/table/Table";
import {
  DateUnitOptions,
  type DateUnitKey,
} from "@/constants/dashboardOptions";
import type { IndexPerformanceResponse } from "@/model/dashboard";
import { useIndexInfoSummaryStore } from "@/store/indexInfoSummaryStore";
import { IndexTrend } from "./IndexTrend";

const IndexPerformance = () => {
  const [selectedUnitId, setSelectedUnitId] = useState<number>(
    DateUnitOptions[2].id,
  );
  const [indexInfoId, setIndexInfoId] = useState<number | null>(null);
  const [rankData, setRankData] = useState<IndexPerformanceResponse[]>([]);
  const [isRankLoading, setIsRankLoading] = useState(false);

  // 지수 목록 스토어 연동
  const {
    items,
    fetch: fetchSummaries,
    isLoading: isSummaryLoading,
  } = useIndexInfoSummaryStore();

  const summaries = useMemo(() => {
    const allOption = { id: 0, label: "지수 전체" };

    if (!Array.isArray(items)) return [allOption];

    const mappedItems = items.map((item) => ({
      id: item.id,
      label: item.indexName,
    }));
    return [allOption, ...mappedItems];
  }, [items]);

  // 현재 선택된 ID에 해당하는 일/주/월간 조회 단위
  const currentPeriodType = (DateUnitOptions.find(
    (opt) => opt.id === selectedUnitId,
  )?.value || "DAILY") as DateUnitKey;

  // 데이터 로드 API
  const fetchRankData = async (period: string, id: number | null) => {
    setIsRankLoading(true);
    try {
      const response = await getIndexPerformanceRank({
        periodType: period as DateUnitKey,
        indexInfoId: id ?? undefined,
        limit: 10,
      });
      const data = Array.isArray(response) ? response : [];
      setRankData(data);
    } catch (error) {
      console.error("랭킹 조회 실패:", error);
      setRankData([]);
    } finally {
      setIsRankLoading(false);
    }
  };

  // 지수 목록 최초 조회
  useEffect(() => {
    void fetchSummaries();
  }, [fetchSummaries]);

  // 필터 변경이나 지수 ID 변경 시 리로딩
  useEffect(() => {
    fetchRankData(currentPeriodType, indexInfoId);
  }, [currentPeriodType, indexInfoId]);

  const handleUnitChange = (id: number) => setSelectedUnitId(id);
  const handleIndexChange = (id: number) =>
    setIndexInfoId(id === 0 ? null : id);

  const hasNoData = !isRankLoading && rankData.length === 0;

  return (
    <div className="border-secondary flex flex-col rounded-xl border bg-white shadow-xs">
      <div className="border-secondary flex w-full justify-between border-b px-6 py-5">
        <span className="text-lg font-semibold">지수 성과</span>
        <div className="flex gap-3">
          <Select
            items={DateUnitOptions}
            aria-label="지수 성과 조회 단위 선택"
            defaultValue={DateUnitOptions[0].id}
            onChange={(key) => handleUnitChange(Number(key))}
            className="w-28"
          >
            {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
          </Select>
          {!isSummaryLoading && (
            <Select
              items={summaries}
              aria-label="지수 선택"
              popoverClassName="scrollbar-thin"
              searchable
              defaultValue={0}
              placeholder="지수 전체"
              onChange={(key) => handleIndexChange(Number(key))}
              className="w-42"
            >
              {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
            </Select>
          )}
        </div>
      </div>
      {isRankLoading ? (
        <div className="text-text-tertiary flex h-72 w-full items-center justify-center">
          로딩 중...
        </div>
      ) : hasNoData ? (
        <p className="text-text-tertiary flex h-72 w-full items-center justify-center text-sm font-semibold">
          데이터가 없습니다.
        </p>
      ) : (
        <Table aria-label="데이터 목록">
          <Table.Header>
            <Table.Head id="rank" label="순위" />
            <Table.Head id="indexName" label="지수" isRowHeader />
            <Table.Head id="versus" label="대비" />
            <Table.Head id="fluctuationRate" label="등락률" />
            <Table.Head id="currentPrice" label="현재" />
            <Table.Head id="beforePrice" label="이전" />
          </Table.Header>

          <Table.Body items={rankData || []}>
            {(item) => (
              <Table.Row
                id={item.performance.indexInfoId}
                key={item.performance.indexInfoId}
              >
                <Table.Cell>{item.rank}</Table.Cell>
                <Table.Cell>
                  <div className="flex flex-col">
                    <p className="text-text-primary text-sm font-medium">
                      {item.performance.indexClassification}
                    </p>
                    <p>{item.performance.indexName}</p>
                  </div>
                </Table.Cell>
                <Table.Cell>
                  <IndexTrend diff={item.performance.versus} />
                </Table.Cell>
                <Table.Cell>
                  <IndexTrend rate={item.performance.fluctuationRate} />
                </Table.Cell>
                <Table.Cell>
                  {item.performance.currentPrice.toLocaleString()}
                </Table.Cell>
                <Table.Cell>
                  {item.performance.beforePrice.toLocaleString()}
                </Table.Cell>
              </Table.Row>
            )}
          </Table.Body>
        </Table>
      )}
    </div>
  );
};

export default IndexPerformance;
