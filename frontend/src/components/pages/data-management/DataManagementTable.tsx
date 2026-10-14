import type { SortDescriptor } from "react-aria-components";
import { Edit01, RefreshCcw05, Trash01 } from "@untitledui/icons";
import { deleteIndexData } from "@/api/indexDataApi";
import { Button } from "@/components/common/buttons/Button";
import { Table } from "@/components/common/table/Table";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import type { IndexDataDto } from "@/model/indexData";
import type { Index } from "@/pages/DataManagement";
import { useIndexDataListStore } from "@/store/indexDataListStore";
import { useModalStore } from "@/store/modalStore";
import { useToastStore } from "@/store/toastStore";
import { isActiveSortColumn } from "@/utils/sort";
import { Badge } from "../../common/badges/Badge";
import { Empty } from "../../common/Empty";
import { IndexTrend } from "../dashboard/IndexTrend";

interface DataManagementTableProps {
  index: Index | null;
}

export default function DataManagementTable({
  index,
}: DataManagementTableProps) {
  const { items, isLoading, error, hasNext, filters, fetch, fetchNext, setFilters } =
    useIndexDataListStore();
  const { successToast, errorToast } = useToastStore();
  const { openConfirm, openIndexDataForm, openIndexDataSync, close } =
    useModalStore();

  // 무한 스크롤 유틸
  const { loadMoreRef } = useInfiniteScroll({
    hasNext,
    isLoading,
    onLoadMore: fetchNext,
    rootMargin: "0px 0px 200px 0px",
  });

  // 컬럼명 → sortField 매핑
  const COLUMN_TO_SORT_FIELD: Record<string, string> = {
    date: "baseDate",
    marketPrice: "marketPrice",
    closingPrice: "closingPrice",
    highPrice: "highPrice",
    lowPrice: "lowPrice",
    tradingQuantity: "tradingQuantity",
    versus: "versus",
    fluctuationRate: "fluctuationRate",
  };

  const SORT_FIELD_TO_COLUMN: Record<string, string> = {
    baseDate: "date",
    marketPrice: "marketPrice",
    closingPrice: "closingPrice",
    highPrice: "highPrice",
    lowPrice: "lowPrice",
    tradingQuantity: "tradingQuantity",
    versus: "versus",
    fluctuationRate: "fluctuationRate",
  };

  // filters에서 sortDescriptor 파생
  const sortDescriptor: SortDescriptor = {
    column: SORT_FIELD_TO_COLUMN[filters.sortField || "baseDate"] || "date",
    direction: filters.sortDirection === "asc" ? "ascending" : "descending",
  };

  // 정렬 변경 핸들러
  const handleSortChange = (descriptor: SortDescriptor) => {
    if (!descriptor.column) return;

    const columnName = descriptor.column as string;
    const sortField = COLUMN_TO_SORT_FIELD[columnName] as
      | "baseDate"
      | "marketPrice"
      | "closingPrice"
      | "highPrice"
      | "lowPrice"
      | "versus"
      | "fluctuationRate"
      | "tradingQuantity"
      | "tradingPrice"
      | "marketTotalAmount";

    if (!sortField) return;

    const sortDirection = descriptor.direction === "ascending" ? "asc" : "desc";

    setFilters({ sortField, sortDirection });
  };

  // 지수 데이터 삭제
  const handleDeleteIndexData = async (id: number) => {
    if (!id) return;
    try {
      await deleteIndexData(id);
      successToast("성공적으로 삭제되었습니다.");
      await fetch();
    } catch (error) {
      errorToast("삭제에 실패하였습니다.");
    } finally {
      close();
    }
  };

  // 지수 상세 모달 열기
  const handleRowClick = (indexData: IndexDataDto) => {
    openIndexDataForm({
      mode: "view",
      initial: indexData,
    });
  };

  // 지수 삭제 핸들러
  const handleDeleteClick = (id: number) => {
    openConfirm({
      title: "지수 데이터 삭제",
      description: "정말로 이 지수 데이터를 삭제하겠습니까?",
      onConfirm: () => {
        handleDeleteIndexData(id);
      },
    });
  };

  // 지수 수정 모달 열기
  const handleEditClick = (indexData: IndexDataDto) => {
    openIndexDataForm({
      mode: "edit",
      initial: indexData,
    });
  };

  // 지수 연동 클릭
  const handleSyncApiClick = () => {
    if (!index) return;
    openIndexDataSync({
      index: index,
    });
  };

  const hasNoData = !isLoading && !error && items.length === 0;

  return (
    <div className="scrollbar-thin flex flex-1 flex-col overflow-auto">
      <Table
        aria-label="데이터 목록"
        sortDescriptor={sortDescriptor}
        onSortChange={handleSortChange}
        selectionMode="none"
      >
        <Table.Header>
          <Table.Head
            id="date"
            label="날짜"
            isRowHeader
            allowsSorting
            isActive={isActiveSortColumn("date", sortDescriptor)}
          />
          <Table.Head
            id="marketPrice"
            label="시가"
            allowsSorting
            isActive={isActiveSortColumn("marketPrice", sortDescriptor)}
          />
          <Table.Head
            id="closingPrice"
            label="종가"
            allowsSorting
            isActive={isActiveSortColumn("closingPrice", sortDescriptor)}
          />
          <Table.Head
            id="highPrice"
            label="고가"
            allowsSorting
            isActive={isActiveSortColumn("highPrice", sortDescriptor)}
          />
          <Table.Head
            id="lowPrice"
            label="저가"
            allowsSorting
            isActive={isActiveSortColumn("lowPrice", sortDescriptor)}
          />
          <Table.Head
            id="tradingQuantity"
            label="거래량"
            allowsSorting
            isActive={isActiveSortColumn("tradingQuantity", sortDescriptor)}
          />
          <Table.Head
            id="versus"
            label="대비"
            allowsSorting
            isActive={isActiveSortColumn("versus", sortDescriptor)}
          />
          <Table.Head
            id="fluctuationRate"
            label="등락률"
            allowsSorting
            isActive={isActiveSortColumn("fluctuationRate", sortDescriptor)}
          />
          <Table.Head id="sourceType" label="소스 타입" />
          <Table.Head id="actions" />
        </Table.Header>

        <Table.Body items={items}>
          {(item) => (
            <Table.Row
              id={item.id}
              key={item.id}
              onAction={() => handleRowClick(item)}
              className="hover:cursor-pointer"
            >
              <Table.Cell className="min-w-35">{item.baseDate}</Table.Cell>
              <Table.Cell>
                {item.marketPrice.toLocaleString("ko-KR")}
              </Table.Cell>
              <Table.Cell>{item.closingPrice}</Table.Cell>
              <Table.Cell>{item.highPrice.toLocaleString("ko-KR")}</Table.Cell>
              <Table.Cell>{item.lowPrice.toLocaleString("ko-KR")}</Table.Cell>
              <Table.Cell>
                {item.tradingQuantity.toLocaleString("ko-KR")}
              </Table.Cell>
              <Table.Cell>
                <IndexTrend diff={item.versus} />
              </Table.Cell>
              <Table.Cell>
                <IndexTrend rate={item.fluctuationRate} />
              </Table.Cell>
              <Table.Cell>
                <Badge kind="source" value={item.sourceType} />
              </Table.Cell>
              <Table.Cell className="w-26">
                <div className="inline-flex justify-end gap-2">
                  <Button
                    color="tertiary"
                    iconLeading={Trash01}
                    className="size-7 text-gray-400"
                    onClick={() => handleDeleteClick(item.id)}
                  />
                  <Button
                    color="tertiary"
                    iconLeading={Edit01}
                    className="size-7 text-gray-400"
                    onClick={() => handleEditClick(item)}
                  />
                </div>
              </Table.Cell>
            </Table.Row>
          )}
        </Table.Body>
      </Table>

      {hasNext && <div ref={loadMoreRef} className="h-4" />}

      <div className="flex flex-col items-center justify-center gap-1 py-2 text-center text-sm text-gray-600">
        {error && <span className="text-red-500">{error.message}</span>}
      </div>

      {hasNoData && (
        <div className="flex flex-1 flex-col items-center justify-center">
          <Empty
            message="등록된 데이터가 없습니다"
            button={
              <Button
                iconLeading={<RefreshCcw05 size={20} />}
                onClick={handleSyncApiClick}
              >
                Open API 연동
              </Button>
            }
          />
        </div>
      )}
    </div>
  );
}
