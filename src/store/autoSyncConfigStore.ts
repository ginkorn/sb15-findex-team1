import { create } from "zustand";
import { getAutoSyncConfigs } from "@/api/syncApi";
import type { AutoSyncConfigDto, AutoSyncConfigParams } from "@/model/sync";

interface AutoSyncConfigListState {
  items: AutoSyncConfigDto[];
  isLoading: boolean;
  error: Error | null;
  hasNext: boolean;
  idAfter: number;
  nextCursor: string | null;
  totalElements: number;
  filters: AutoSyncConfigParams;

  fetch: (params?: AutoSyncConfigParams) => Promise<void>;
  fetchNext: () => Promise<void>;
  setFilters: (filters: Partial<AutoSyncConfigParams>) => void;
  resetFilters: () => void;
  // 낙관적 상태 업데이트
  updateItemEnabled: (id: number, enabled: boolean) => void;
}

export const useAutoSyncConfigListStore = create<AutoSyncConfigListState>(
  (set, get) => ({
    items: [],
    isLoading: false,
    error: null,
    hasNext: false,
    idAfter: 0,
    nextCursor: null,
    totalElements: 0,
    filters: {},

    fetch: async (params) => {
      if (get().isLoading) return;
      set({ isLoading: true, error: null });

      if (params) {
        set({ filters: params });
      }

      const filters = params || get().filters;

      try {
        const page = await getAutoSyncConfigs(filters);
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
        const page = await getAutoSyncConfigs({
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
        filters: {},
      });
    },

    updateItemEnabled: (id: number, enabled: boolean) =>
      set((state) => ({
        items: state.items.map((item) =>
          item.id === id ? { ...item, enabled } : item,
        ),
      })),
  }),
);
