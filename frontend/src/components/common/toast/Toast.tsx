import type { ToastType } from "@/types/toast";
import { cx } from "@/utils/cx";

interface ToastProps {
  type: ToastType;
  message: string;
  onClose: () => void;
}

export default function Toast({ type, message }: ToastProps) {
  const isSuccess = type === "success";

  return (
    <div
      className="pointer-events-auto flex w-fit items-center gap-2.5 rounded-xl bg-black px-12 py-4 text-white"
      role={isSuccess ? "status" : "alert"}
      aria-live={isSuccess ? "polite" : "assertive"}
    >
      <div
        className={cx(
          "h-2.5 w-2.5 rounded-full",
          isSuccess ? "bg-green-500" : "bg-red-600",
        )}
      />

      <p className="text-md flex-1 leading-6 font-semibold">{message}</p>
    </div>
  );
}
