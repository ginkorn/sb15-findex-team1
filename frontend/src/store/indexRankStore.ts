import { create } from "zustand";
import { getIndexPerformanceRank } from "@/api/indexDataApi";
import type {
  RankedIndexPerformanceDto,
  UnitPeriodType,
} from "@/model/indexData";

interface PerformanceRankFilters {
  indexInfoId?: number;
  periodType?: UnitPeriodType;
  limit?: number;
}

interface PerformanceRankState {
  data: RankedIndexPerformanceDto[] | null;
  isLoading: boolean;
  error: Error | null;
  filters: PerformanceRankFilters;

  fetch: (params?: PerformanceRankFilters) => Promise<void>;
  setFilters: (filters: Partial<PerformanceRankFilters>) => void;
  reset: () => void;
}

export const usePerformanceRankStore = create<PerformanceRankState>(
  (set, get) => ({
    data: null,
    isLoading: false,
    error: null,
    filters: { periodType: "DAILY", limit: 10 },

    fetch: async (params) => {
      set({ isLoading: true, error: null });

      if (params) {
        set({ filters: params });
      }

      const filters = params || get().filters;

      try {
        const data = await getIndexPerformanceRank(filters);
        set({ data, isLoading: false });
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
        data: null,
        isLoading: false,
        error: null,
        filters: { periodType: "DAILY", limit: 10 },
      });
    },
  }),
);
