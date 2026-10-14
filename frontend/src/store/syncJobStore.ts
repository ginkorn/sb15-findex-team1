import { create } from "zustand";
import { getSyncJobs } from "@/api/syncApi";
import type { SyncJobDto, SyncJobParams } from "@/model/sync";
import { formatDateToYmdHms } from "@/utils/date";

interface SyncJobListState {
  items: SyncJobDto[];
  isLoading: boolean;
  isLoadingStats: boolean;
  error: Error | null;
  hasNext: boolean;
  idAfter: number;
  nextCursor: string | null;
  totalElements: number;
  totalSuccess: number;
  totalFailed: number;
  latestSync: string | null;
  filters: SyncJobParams;

  fetch: (params?: SyncJobParams) => Promise<void>;
  fetchNext: () => Promise<void>;
  setFilters: (filters: Partial<SyncJobParams>) => void;
  resetFilters: () => void;

  fetchStats: () => Promise<void>;
}

const initialFilters: SyncJobParams = {
  sortField: "jobTime",
  sortDirection: "desc",
  size: 20
};

export const useSyncJobListStore = create<SyncJobListState>((set, get) => ({
  items: [],
  isLoading: false,
  isLoadingStats: false,
  error: null,
  hasNext: false,
  idAfter: 0,
  nextCursor: null,
  totalElements: 0,
  totalSuccess: 0,
  totalFailed: 0,
  latestSync: null,
  filters: initialFilters,

  fetch: async (params) => {
    if (get().isLoading) return;
    set({ isLoading: true, error: null });

    if (params) {
      set({ filters: params });
    }

    const filters = params || get().filters;

    try {
      const page = await getSyncJobs(filters);
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
      const page = await getSyncJobs({
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

  fetchStats: async () => {
    set({ isLoadingStats: true, error: null });

    try {
      const todayDate = new Date();
      const targetDate = new Date();
      targetDate.setDate(targetDate.getDate() - 7); // 오늘로부터 7일 전 설정

      const jobTimeFrom = formatDateToYmdHms(targetDate);
      const jobTimeTo = formatDateToYmdHms(todayDate);

      const [successRes, failedRes] = await Promise.all([
        getSyncJobs({
          status: "SUCCESS",
          sortField: "jobTime",
          sortDirection: "desc",
          jobTimeFrom,
          jobTimeTo,
        }),
        getSyncJobs({
          status: "FAILED",
          sortField: "jobTime",
          sortDirection: "desc",
          jobTimeFrom,
          jobTimeTo,
        }),
      ]);

      set({
        isLoadingStats: false,
        totalSuccess: successRes.totalElements,
        totalFailed: failedRes.totalElements,
        latestSync: successRes.content[0].jobTime ?? null,
      });
    } catch (error) {
      set({ error: error as Error, isLoadingStats: false });
    }
  },
}));
