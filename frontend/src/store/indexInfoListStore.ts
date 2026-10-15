import { create } from "zustand";
import { getIndexInfoList } from "@/api/indexInfoApi";
import type { IndexInfoListParams, IndexInfoResponse } from "@/model/indexInfo";

interface IndexInfoListState {
  items: IndexInfoResponse[];
  isLoading: boolean;
  error: Error | null;
  hasNext: boolean;
  idAfter: number;
  nextCursor: string | null;
  totalElements: number;
  filters: IndexInfoListParams;

  fetch: (params?: IndexInfoListParams) => Promise<void>;
  fetchNext: () => Promise<void>;
  setFilters: (filters: Partial<IndexInfoListParams>) => void;
  reset: () => void;
}

export const useIndexIndexListStore = create<IndexInfoListState>(
  (set, get) => ({
    items: [],
    isLoading: false,
    error: null,
    hasNext: false,
    idAfter: 0,
    nextCursor: null,
    totalElements: 0,
    filters: {
      sortField: "indexClassification",
      sortDirection: "desc",
    },

    fetch: async (params) => {
      if (get().isLoading) return;

      set({ isLoading: true, error: null });

      if (params) {
        set({ filters: params });
      }

      const filters = params || get().filters;

      try {
        const page = await getIndexInfoList(filters);
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
        const page = await getIndexInfoList({
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
      set((state) => ({
        filters: { ...state.filters, ...newFilters },
      }));
    },

    reset: () => {
      set({
        items: [],
        isLoading: false,
        error: null,
        filters: {},
      });
    },
  }),
);
