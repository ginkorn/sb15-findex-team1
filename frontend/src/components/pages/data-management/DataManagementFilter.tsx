import { useEffect, useMemo, useState } from "react";
import type { DateRange } from "react-aria-components";
import { DateRangePicker } from "@/components/common/date-picker/DateRangePicker";
import type { Index } from "@/pages/DataManagement";
import { useIndexDataListStore } from "@/store/indexDataListStore";
import { useIndexInfoSummaryStore } from "@/store/indexInfoSummaryStore";
import { formatDateRange } from "@/utils/date";
import { Select } from "../../common/select/Select";

interface DataManagementFilterProps {
  onIndexChange: (index: Index) => void;
}

export default function DataManagementFilter({
  onIndexChange,
}: DataManagementFilterProps) {
  const [tempDateRange, setTempDateRange] = useState<DateRange | null>(null);

  const {
    fetch: fetchIndexList,
    filters,
    setFilters,
    resetFilters,
  } = useIndexDataListStore();
  const { items: indexInfoItems, fetch: fetchIndexSummaries } =
    useIndexInfoSummaryStore();

  // 지수 정보 요약 목록
  const summaries = useMemo(() => {
    const allOption = {
      id: -1,
      label: "전체 지수",
    };

    const mappedItems = indexInfoItems.map((item) => ({
      id: item.id,
      label: item.indexName,
    }));

    return [allOption, ...mappedItems];
  }, [indexInfoItems]);

  const currentIndexId = filters.indexInfoId ?? summaries[0]?.id;

  const handleDateChange = (value: DateRange | null) => {
    setTempDateRange(value);
  };

  // 날짜 범위 적용
  const handleDateApply = () => {
    if (!tempDateRange) return;
    const { start, end } = formatDateRange(tempDateRange);

    setFilters({
      startDate: start,
      endDate: end,
    });
  };

  // 취소 버튼 클릭 시 tempDateRange 초기화
  const handleDateCancel = () => {
    setTempDateRange(null);
    setFilters({
      startDate: "",
      endDate: "",
    });
  };

  // 지수 선택 핸들러
  const handleSelectChange = (id: number) => {
    const selectedItem = summaries.find((item) => item.id === id);
    if (selectedItem) onIndexChange(selectedItem);
    setFilters({ indexInfoId: id });
  };

  useEffect(() => {
    fetchIndexSummaries();
    resetFilters();
    fetchIndexList();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    fetchIndexList();
  }, [filters]);

  return (
    <div className="flex gap-3 px-6 py-5">
      <Select
        items={summaries}
        aria-label="지수 선택"
        placeholder="지수 선택"
        popoverClassName="scrollbar-thin"
        value={currentIndexId}
        searchable
        onChange={(key) => handleSelectChange(key as number)}
        className="w-42"
      >
        {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
      </Select>

      <DateRangePicker
        aria-label="날짜 선택"
        placeholder="날짜를 선택해주세요"
        value={tempDateRange}
        onChange={(value) => handleDateChange(value)}
        onApply={handleDateApply}
        onCancel={handleDateCancel}
        className="min-w-66"
      />
    </div>
  );
}
