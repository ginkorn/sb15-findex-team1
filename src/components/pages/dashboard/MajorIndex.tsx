import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowRight } from "@untitledui/icons";
import { getMajorIndexPerformance } from "@/api/dashboardApi";
import { Button } from "@/components/common/buttons/Button";
import { Select } from "@/components/common/select/Select";
import {
  DateUnitOptions,
  type DateUnitKey,
} from "@/constants/dashboardOptions";
import type { MajorIndexResponse } from "@/model/dashboard";
import IndexCard from "./IndexCard";

const MajorIndex = () => {
  const navigate = useNavigate();
  const [selectedId, setSelectedId] = useState<number>(DateUnitOptions[2].id);
  const [summaries, setSummaries] = useState<MajorIndexResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // 선택된 ID로 백엔드용 PeriodType 키 도출
  const selectedValue = DateUnitOptions.find((opt) => opt.id === selectedId)
    ?.value as DateUnitKey;

  // api fetch
  const fetchMajorIndexes = async (period: DateUnitKey) => {
    setIsLoading(true);
    try {
      const response = await getMajorIndexPerformance({
        periodType: period,
      });
      setSummaries(response);
    } catch (error) {
      console.error("주요 지수 데이터 로드 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMajorIndexes(selectedValue);
  }, [selectedValue]);

  const handleUnitChange = (id: number) => {
    setSelectedId(id);
  };

  const onClickMove = () => {
    navigate("/index-management");
  };

  return (
    <div className="border-secondary flex flex-col rounded-xl border bg-white shadow-xs">
      <div className="border-secondary flex w-full justify-between border-b px-6 py-5">
        <span className="text-lg font-semibold">주요 지수</span>
        <Select
          items={DateUnitOptions}
          aria-label="주요 지수 조회 단위 선택"
          defaultValue={DateUnitOptions[2].id}
          onChange={(key) => handleUnitChange(Number(key))}
          className="w-28"
        >
          {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
        </Select>
      </div>
      <div className="scrollbar-thin flex w-full gap-4 overflow-x-auto scroll-smooth p-6">
        {isLoading ? (
          <div className="text-text-tertiary flex h-44 w-full items-center justify-center">
            데이터를 불러오는 중입니다...
          </div>
        ) : (summaries || []).length > 0 ? (
          (summaries || []).map((indexData) => (
            <IndexCard key={indexData.indexInfoId} data={indexData} />
          ))
        ) : (
          <div className="text-text-tertiary flex h-44 w-full flex-col items-center justify-center gap-4 text-sm">
            <p>지수를 즐겨찾기에 등록해주세요.</p>
            <Button
              iconTrailing={<ArrowRight size={20} stroke="#A4A7AE" />}
              color="secondary"
              showTextWhileLoading
              onClick={onClickMove}
            >
              지수 관리하러 가기
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default MajorIndex;
