
import { useEffect, useMemo, useState } from "react";
import type { DateRange } from "react-aria-components";
import { syncIndexData } from "@/api/indexDataApi";
import type { IndexDataSyncRequest } from "@/model/indexData";
import type { Index } from "@/pages/DataManagement";
import { useIndexDataListStore } from "@/store/indexDataListStore";
import { useIndexInfoSummaryStore } from "@/store/indexInfoSummaryStore";
import { useToastStore } from "@/store/toastStore";
import { formatDateValue } from "@/utils/date";
import { DateRangePicker } from "../../common/date-picker/DateRangePicker";
import { Label } from "../../common/input/Label";
import ConfirmModal from "../../common/modals/ConfirmModal";
import { Select } from "../../common/select/Select";

interface IndexDataSyncModalProps {
  isOpen: boolean;
  onClose: () => void;
  index: Index;
}

interface Counts {
  SUCCESS: number;
  FAILED: number;
}

export default function IndexDataSyncModal({
                                             isOpen,
                                             onClose,
                                             index,
                                           }: IndexDataSyncModalProps) {

  // 연동할 날짜 범위
  const [dateRange, setDateRange] = useState<DateRange | null>(null);

  // 연동 성공 및 실패 건수
  const [counts, setCounts] = useState<Counts | null>(null);

  // 연동 완료 여부
  const [isSynced, setIsSynced] = useState(false);

  // 연동할 지수 ID
  // -1은 전체 지수를 의미
  const [selectedIndexId, setSelectedIndexId] = useState(index.id);

  // API 연동 진행 상태
  const [isSyncing, setIsSyncing] = useState(false);

  const { successToast, errorToast } = useToastStore();
  const { fetch } = useIndexDataListStore();

  // A 파트에서 등록한 지수 정보 목록
  const {
    items: indexInfoItems,
    fetch: fetchIndexSummaries,
    isLoading: isIndexLoading,
    error: indexError,
  } = useIndexInfoSummaryStore();

  // 드롭다운에 표시할 지수 목록
  const selectableIndexes = useMemo(() => {
    return [
      {
        id: -1,
        label: "전체 지수",
      },
      ...indexInfoItems.map((item) => ({
        id: item.id,
        label: `${item.indexClassification} / ${item.indexName}`,
      })),
    ];
  }, [indexInfoItems]);

  // 모달이 열리면 등록된 지수 목록 조회
  useEffect(() => {
    if (isOpen) {
      void fetchIndexSummaries();
    }
  }, [isOpen, fetchIndexSummaries]);

  // 날짜 변경
  const handleDateChange = (value: DateRange | null) => {
    setDateRange(value);
  };

  // 날짜 선택 취소
  const handleDateCancel = () => {
    setDateRange(null);
  };

  // Open API 연동 실행
  const handleSyncClick = async () => {
    if (!dateRange || isSyncing) return;

    const requestBody: IndexDataSyncRequest = {

      // 전체 지수라면 빈 배열 전달
      // 특정 지수라면 해당 지수 ID 전달
      indexInfoIds:
          selectedIndexId === -1 ? [] : [selectedIndexId],

      baseDateFrom: formatDateValue(dateRange.start),
      baseDateTo: formatDateValue(dateRange.end),
    };

    setIsSyncing(true);

    try {
      // 백엔드에 연동 요청
      const data = await syncIndexData(requestBody);

      // 성공 및 실패 건수 계산
      const resultCounts = data.reduce(
          (acc, cur) => {
            if (cur.result === "SUCCESS") {
              acc.SUCCESS += 1;
            } else if (cur.result === "FAILED") {
              acc.FAILED += 1;
            }
            return acc;
          },
          { SUCCESS: 0, FAILED: 0 },
      );

      // 데이터 관리 목록 새로고침
      await fetch();

      setCounts(resultCounts);
      setIsSynced(true);

      successToast("연동이 완료되었습니다.");

    } catch (error) {
      errorToast("연동 중 오류가 발생하였습니다.");

    } finally {
      setIsSyncing(false);
    }
  };

  return (
      <ConfirmModal
          isOpen={isOpen}
          onClose={onClose}
          variant="primary"
          title="지수 데이터 연동"
          confirmText={isSynced ? "확인" : "연동"}
          onConfirm={isSynced ? onClose : handleSyncClick}
          isDisabled={
              !isSynced &&
              (
                  !dateRange ||
                  isSyncing ||
                  isIndexLoading ||
                  !!indexError ||
                  indexInfoItems.length === 0
              )
          }
          isLoading={isSyncing}
          isSingleButton={isSynced}
      >
        {!isSynced ? (
            <div className="flex w-full flex-col gap-2">

              <p className="text-tertiary mb-6 text-sm font-normal">
                Open API를 통해 지수 데이터를 연동합니다.
              </p>

              {/* 연동할 지수 선택 */}
              <div className="mb-4 flex flex-col gap-2">
                <Label aria-label="연동할 지수" isRequired>
                  연동할 지수
                </Label>

                <Select
                    items={selectableIndexes}
                    aria-label="연동할 지수 선택"
                    placeholder="지수를 선택해주세요"
                    popoverClassName="scrollbar-thin"
                    value={selectedIndexId}
                    searchable
                    onChange={(key) =>
                        setSelectedIndexId(key as number)
                    }
                    className="w-full"
                >
                  {(item) => (
                      <Select.Item id={item.id}>
                        {item.label}
                      </Select.Item>
                  )}
                </Select>
              </div>

              {/* 대상 날짜 선택 */}
              <Label aria-label="날짜" isRequired>
                대상 날짜
              </Label>

              <DateRangePicker
                  aria-label="날짜"
                  placeholder="날짜를 선택해주세요"
                  value={dateRange}
                  onChange={handleDateChange}
                  onCancel={handleDateCancel}
                  className="w-full"
              />

            </div>
        ) : (

            // 연동 결과 화면
            <div className="text-tertiary flex flex-col text-sm font-normal">

              <div className="flex items-center gap-2">
                <div className="h-2 w-2 rounded-full bg-green-500" />
                성공: {counts?.SUCCESS}
              </div>

              <div className="flex items-center gap-2">
                <div className="h-2 w-2 rounded-full bg-red-600" />
                실패: {counts?.FAILED}
              </div>

            </div>
        )}
      </ConfirmModal>
  );
}
