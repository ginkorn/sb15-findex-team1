import { useToastStore } from "@/store/toastStore";
import Toast from "./Toast";

/**
 * 전역 토스트 렌더러
 */
export function ToastContainer() {
  const toast = useToastStore((state) => state.toast);
  const clearToast = useToastStore((state) => state.clearToast);

  if (!toast) return null;

  return (
    <div className="pointer-events-none fixed bottom-21 left-1/2 z-9999 -translate-x-1/2">
      <Toast
        type={toast.type}
        message={toast.message}
        onClose={() => clearToast()}
      />
    </div>
  );
}
