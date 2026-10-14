import { create } from "zustand";
import { getFavoriteIndexPerformance } from "@/api/indexDataApi";
import type { IndexPerformanceDto, UnitPeriodType } from "@/model/indexData";

interface FavoritePerformanceState {
  data: IndexPerformanceDto[];
  isLoading: boolean;
  error: Error | null;
  periodType: UnitPeriodType;

  fetch: (periodType?: UnitPeriodType) => Promise<void>;
  setPeriodType: (periodType: UnitPeriodType) => void;
  reset: () => void;
}

export const useFavoritePerformanceStore = create<FavoritePerformanceState>(
  (set, get) => ({
    data: [],
    isLoading: false,
    error: null,
    periodType: "DAILY",

    fetch: async (periodType) => {
      set({ isLoading: true, error: null });

      const type = periodType || get().periodType;
      if (periodType) {
        set({ periodType });
      }

      try {
        const data = await getFavoriteIndexPerformance(type);
        set({ data, isLoading: false });
      } catch (error) {
        set({ error: error as Error, isLoading: false });
      }
    },

    setPeriodType: (periodType) => {
      set({ periodType });
    },

    reset: () => {
      set({
        data: [],
        isLoading: false,
        error: null,
        periodType: "DAILY",
      });
    },
  }),
);
