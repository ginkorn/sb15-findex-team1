import { create } from "zustand";
import { downloadIndexDataCSV, getIndexDataList } from "@/api/indexDataApi";
import type { IndexDataDto, IndexDataListParams } from "@/model/indexData";

interface IndexDataListState {
  items: IndexDataDto[];
  isLoading: boolean;
  isLoadingExport: boolean;
  error: Error | null;
  hasNext: boolean;
  idAfter: number;
  nextCursor: string | null;
  totalElements: number;
  filters: IndexDataListParams;

  fetch: (params?: IndexDataListParams) => Promise<void>;
  fetchNext: () => Promise<void>;
  setFilters: (filters: Partial<IndexDataListParams>) => void;
  resetFilters: () => void;
  exportData: (params?: IndexDataListParams) => Promise<void>;
}

const initialFilters: IndexDataListParams = {
  sortField: "baseDate",
  sortDirection: "desc",
};

export const useIndexDataListStore = create<IndexDataListState>((set, get) => ({
  items: [],
  isLoading: false,
  isLoadingExport: false,
  error: null,
  hasNext: false,
  idAfter: 0,
  nextCursor: null,
  totalElements: 0,
  filters: initialFilters,

  fetch: async (params) => {
    if (get().isLoading) return;
    set({ isLoading: true, error: null });

    if (params) {
      set({ filters: params });
    }

    const filters = params || get().filters;

    try {
      const page = await getIndexDataList(filters);
      set({
        items: page.content,
        isLoading: false,
        hasNext: page.hasNext,
        idAfter: page.nextIdAfter,
        nextCursor: page.nextCursor,
        totalElements: page.totalElements,
      });
    } catch (error) {
      set({ error: error as Error, isLoading: false });
    }
  },

  fetchNext: async () => {
    const { items, filters, isLoading, hasNext, nextCursor, idAfter } = get();

    if (isLoading || !hasNext || !nextCursor) {
      return;
    }

    set({ isLoading: true, error: null });

    try {
      const page = await getIndexDataList({
        ...filters,
        cursor: nextCursor,
        idAfter: idAfter,
      });

      set({
        items: [...items, ...page.content],
        isLoading: false,
        nextCursor: page.nextCursor,
        idAfter: page.nextIdAfter,
        hasNext: page.hasNext,
      });
    } catch (error) {
      set({ error: error as Error, isLoading: false });
    }
  },

  setFilters: (newFilters) => {
    set((state) => {
      const updatedFilters = { ...state.filters, ...newFilters };

      // indexInfoId가 -1이면 제거
      if (updatedFilters.indexInfoId === -1) {
        delete updatedFilters.indexInfoId;
      }

      return { filters: updatedFilters };
    });
  },

  resetFilters: () => {
    set({
      filters: initialFilters,
    });
  },

  exportData: async () => {
    const { filters, isLoading, isLoadingExport } = get();

    if (isLoading || isLoadingExport) {
      return;
    }

    set({ isLoadingExport: true });

    const params: Partial<IndexDataListParams> = {
      indexInfoId: filters.indexInfoId,
      startDate: filters.startDate,
      endDate: filters.endDate,
      sortField: filters.sortField,
      sortDirection: filters.sortDirection,
    };

    try {
      const csvText = await downloadIndexDataCSV(params);

      // 문자열을 Blob으로 변환
      const blob = new Blob([csvText], { type: "text/csv;charset=utf-8;" });

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;

      const fileName = `index-data-${new Date().toISOString().split("T")[0]}.csv`;
      link.download = fileName;

      document.body.appendChild(link);
      link.click();

      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      set({ isLoadingExport: false });
    } catch (error) {
      set({ isLoadingExport: false });
      throw error;
    }
  },
}));
