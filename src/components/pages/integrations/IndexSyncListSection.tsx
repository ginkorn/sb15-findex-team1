import { useEffect, useMemo } from "react";
import { patchAutoSyncConfigs } from "@/api/syncApi";
import { IndexJobStatusOptions } from "@/constants/integrationsLabels";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import type { AutoSyncConfigDto } from "@/model/sync";
import { useAutoSyncConfigListStore } from "@/store/autoSyncConfigStore";
import { useIndexInfoSummaryStore } from "@/store/indexInfoSummaryStore";
import { Empty } from "../../common/Empty";
import { Select } from "../../common/select/Select";
import { Table } from "../../common/table/Table";
import { Toggle } from "../../common/toggle/toggle";

export const IndexSyncListSection = () => {
  const { items: indexInfoItems, fetch: fetchInfoItems } =
    useIndexInfoSummaryStore();
  const {
    items: indexItems,
    fetch: fetchIndexItems,
    fetchNext: fetchNextIndexItems,
    isLoading: isLoadingIndexItems,
    hasNext,
    filters,
    setFilters,
    resetFilters,
    updateItemEnabled,
  } = useAutoSyncConfigListStore();

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

  const currentIndexId = filters.indexInfoId ?? -1;
  const currentStatusId =
    filters.enabled === undefined
      ? 0
      : (IndexJobStatusOptions.find((opt) => opt.value === filters.enabled)
          ?.id ?? 0);

  // 무한 스크롤 유틸
  const { loadMoreRef } = useInfiniteScroll({
    hasNext,
    isLoading: isLoadingIndexItems,
    onLoadMore: fetchNextIndexItems,
    rootMargin: "0px 0px 200px 0px",
  });

  // 지수 선택 핸들러
  const handleIndexSelectChange = (id: number) => {
    const selectedItem = summaries.find((item) => item.id === id);
    setFilters({
      indexInfoId: selectedItem?.id,
    });
  };

  // 지수 상태 선택
  const handleIndexStatusChange = (id: number) => {
    const selectedItem = IndexJobStatusOptions.find(
      (item) => item.id === Number(id),
    );
    const value = selectedItem?.value;

    if (value === "") {
      setFilters({ enabled: undefined });
    } else {
      setFilters({ enabled: value as boolean });
    }
  };

  // 지수 활성화 토글
  const handleIndexStatusToggle = async (
    item: AutoSyncConfigDto,
    newEnabledValue: boolean,
  ) => {
    const previousValue = item.enabled;
    updateItemEnabled(item.id, newEnabledValue);

    const body = {
      enabled: newEnabledValue,
    };

    try {
      await patchAutoSyncConfigs(item.id, body);
      await fetchIndexItems();
    } catch (error) {
      updateItemEnabled(item.id, previousValue);
    }
  };

  // 지수 정보 요약 목록 조회
  useEffect(() => {
    void fetchInfoItems();
  }, [fetchInfoItems]);

  // 자동 연동 지수 목록 조회
  useEffect(() => {
    void fetchIndexItems();
  }, [fetchIndexItems, filters]);

  useEffect(() => {
    resetFilters();
  }, []);

  return (
    <section className="border-secondary flex w-87 shrink-0 flex-col overflow-hidden rounded-xl border bg-white shadow-xs">
      <div className="border-secondary border-b px-6 py-5">
        <h2 className="text-lg leading-7 font-semibold">자동 연동 지수 목록</h2>
      </div>
      <div className="border-secondary flex gap-3 border-b px-6 py-5">
        <Select
          items={summaries}
          aria-label="지수 선택"
          placeholder="지수 선택"
          popoverClassName="scrollbar-thin"
          value={currentIndexId}
          searchable
          onChange={(key) => handleIndexSelectChange(key as number)}
          className="w-36"
        >
          {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
        </Select>
        <Select
          items={IndexJobStatusOptions}
          aria-label="지수 연동 상태 선택"
          value={currentStatusId}
          onChange={(key) => handleIndexStatusChange(key as number)}
          className="w-36"
        >
          {(item) => <Select.Item id={item.id}>{item.label}</Select.Item>}
        </Select>
      </div>
      <div className="scrollbar-thin flex min-h-0 flex-1 flex-col overflow-y-auto bg-white">
        <Table aria-label="자동 연동 지수 목록" selectionMode="none">
          <Table.Header>
            <Table.Head id="index" label="지수" isRowHeader />
            <Table.Head id="status" label="활성화" />
          </Table.Header>
          <Table.Body items={indexItems}>
            {(item) => (
              <Table.Row id={item.id} key={item.id}>
                <Table.Cell>
                  <div>
                    <p className="text-sm leading-5 font-medium">
                      {item.indexName}
                    </p>
                    <p className="text-quaternary text-sm leading-5 font-normal">
                      {item.indexClassification}
                    </p>
                  </div>
                </Table.Cell>
                <Table.Cell className="w-26">
                  <Toggle
                    isSelected={item.enabled}
                    onChange={(isSelected) =>
                      handleIndexStatusToggle(item, isSelected)
                    }
                  />
                </Table.Cell>
              </Table.Row>
            )}
          </Table.Body>
        </Table>

        {hasNext && <div ref={loadMoreRef} className="h-4" />}

        {!isLoadingIndexItems && indexItems.length === 0 && (
          <div className="flex flex-1 flex-col items-center justify-center">
            <Empty message="지수 정보가 없습니다" />
          </div>
        )}
      </div>
    </section>
  );
};
