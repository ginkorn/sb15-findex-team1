import { create } from "zustand";
import { getIndexChart } from "@/api/indexDataApi";
import type { IndexChartDto, PeriodType } from "@/model/indexData";

interface IndexChartState {
  data: IndexChartDto | null;
  isLoading: boolean;
  error: Error | null;
  filters: { id: number; periodType: PeriodType } | null;

  fetch: (id: number, periodType?: PeriodType) => Promise<void>;
  setFilters: (filters: { id: number; periodType: PeriodType }) => void;
  reset: () => void;
}

export const useIndexChartStore = create<IndexChartState>((set) => ({
  data: null,
  isLoading: false,
  error: null,
  filters: null,

  fetch: async (id, periodType = "MONTHLY") => {
    set({ isLoading: true, error: null, filters: { id, periodType } });

    try {
      const data = await getIndexChart(id, periodType);
      set({ data, isLoading: false });
    } catch (error) {
      set({ error: error as Error, isLoading: false });
    }
  },

  setFilters: (filters) => {
    set({ filters });
  },

  reset: () => {
    set({
      data: null,
      isLoading: false,
      error: null,
      filters: null,
    });
  },
}));
