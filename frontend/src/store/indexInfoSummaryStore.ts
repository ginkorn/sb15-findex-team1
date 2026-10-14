import { create } from "zustand";
import { getIndexInfoSummaries } from "@/api/indexInfoApi";
import type { IndexInfoSummary } from "@/model/indexInfo";

interface IndexInfoSummaryState {
  items: IndexInfoSummary[];
  isLoading: boolean;
  error: Error | null;
  fetch: () => Promise<void>;
}

export const useIndexInfoSummaryStore = create<IndexInfoSummaryState>(
  (set, _get) => ({
    items: [],
    isLoading: false,
    error: null,
    fetch: async () => {
      if (_get().isLoading) return;
      set({ isLoading: true, error: null });

      try {
        const data = await getIndexInfoSummaries();
        set({
          items: Array.isArray(data) ? data : [],
          isLoading: false,
        });
      } catch (error) {
        set({ error: error as Error, isLoading: false, items: [] });
      }
    },
  }),
);
