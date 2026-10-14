import type { ToastType } from "@/types/toast";
import { create } from "zustand";

interface ToastState {
  id: number;
  type: ToastType;
  message: string;
}

interface ToastStore {
  toast: ToastState | null;
  showToast: (type: ToastType, message: string, duration?: number) => void;
  successToast: (message: string, duration?: number) => void;
  errorToast: (message: string, duration?: number) => void;
  clearToast: (id?: number) => void;
}

/**
 * 전역 토스트 zustand 스토어
 */
export const useToastStore = create<ToastStore>((set, get) => ({
  toast: null,
  // 토스트 제거
  clearToast: (id) => {
    const current = get().toast;
    if (!current) return;

    if (id !== undefined && current.id !== id) return;

    set({ toast: null });
  },
  // 토스트 표시
  showToast: (type, message, duration = 3000) => {
    const id = Date.now();

    set({
      toast: {
        id,
        type,
        message,
      },
    });

    window.setTimeout(() => {
      get().clearToast(id);
    }, duration);
  },
  // 성공 토스트 표시
  successToast: (message, duration) => {
    get().showToast("success", message, duration);
  },
  // 실패 토스트 표시
  errorToast: (message, duration) => {
    get().showToast("error", message, duration);
  },
}));
