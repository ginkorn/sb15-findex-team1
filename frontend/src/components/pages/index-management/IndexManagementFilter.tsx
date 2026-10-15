import { useEffect, useState } from "react";
import { SearchMd } from "@untitledui/icons";
import { DropdownButton } from "@/components/common/dropdown/DropdownButton";
import { Input } from "@/components/common/input/Input";
import {
  convertViewKeyToFavoriteBoolean,
  FavoriteOptions,
} from "@/constants/indexInfoOptions";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useIndexIndexListStore } from "@/store/indexInfoListStore";

const IndexFilterSection = () => {
  const { filters, setFilters } = useIndexIndexListStore();
  // 사용자의 입력을 즉각 반영할 로컬 state
  const [classification, setClassification] = useState(
    filters.indexClassification || "",
  );
  const [indexName, setIndexName] = useState(filters.indexName || "");
  // 디바운스 훅 적용
  const debouncedClassification = useDebouncedValue(classification, 300);
  const debouncedIndexName = useDebouncedValue(indexName, 300);
  // 디바운스된 값이 확정되면 스토어의 필터를 업데이트 (API 호출 트리거)
  useEffect(() => {
    setFilters({
      indexClassification: debouncedClassification,
      indexName: debouncedIndexName,
    });
  }, [debouncedClassification, debouncedIndexName, setFilters]);

  // 즐겨찾기 드롭다운
  const [selectedViewKey, setSelectedViewKey] = useState<string | null>("all");
  const handleViewChange = (key: string | null) => {
    setSelectedViewKey(key);
    // key를 boolean | undefined로 변환
    const favoriteValue = convertViewKeyToFavoriteBoolean(key);
    setFilters({
      favorite: favoriteValue,
    });
  };
  return (
    <div className="border-secondary flex justify-between border-b px-6 py-5">
      <div className="flex justify-start gap-3">
        <Input
          placeholder="분류명"
          className="w-[168px]"
          icon={SearchMd}
          value={classification}
          onChange={setClassification}
        />
        <Input
          placeholder="지수명"
          className="w-[168px]"
          icon={SearchMd}
          value={indexName}
          onChange={setIndexName}
        />
      </div>
      <DropdownButton
        label={FavoriteOptions}
        value={selectedViewKey}
        onChange={handleViewChange}
        placeholder="전체보기"
        className="w-36"
      />
    </div>
  );
};

export default IndexFilterSection;
