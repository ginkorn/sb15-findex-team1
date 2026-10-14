import { cx } from "@/utils/cx";
import { X } from "@untitledui/icons";
import * as React from "react";
import { Dialog, Heading, Modal, ModalOverlay } from "react-aria-components";

export type BaseModalProps = {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  title?: React.ReactNode;
  ariaLabel?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  isDismissable?: boolean;
  className?: string;
  contentClassName?: string;
};

export function BaseModal({
  isOpen,
  onOpenChange,
  title,
  ariaLabel,
  children,
  footer,
  isDismissable = true,
  className,
  contentClassName = "px-5 py-7",
}: BaseModalProps) {
  const computedAriaLabel =
    ariaLabel ?? (typeof title === "string" ? title : undefined) ?? "Dialog";

  // scrollbar-gutter로 생기는 흰색 배경 제거
  React.useEffect(() => {
    if (isOpen) {
      // 현재 스크롤바 너비 계산
      const scrollbarWidth =
        window.innerWidth - document.documentElement.clientWidth;

      // body에 overflow hidden과 padding 추가
      document.body.style.overflow = "hidden";
      document.body.style.paddingRight = `${scrollbarWidth}px`;

      return () => {
        document.body.style.overflow = "";
        document.body.style.paddingRight = "";
      };
    }
  }, [isOpen]);

  return (
    <ModalOverlay
      isOpen={isOpen}
      onOpenChange={onOpenChange}
      isDismissable={isDismissable}
      className={cx(
        "fixed inset-0 z-50 bg-black/70",
        "flex min-h-dvh items-center justify-center p-4 overflow-y-auto",
        "entering:animate-in entering:fade-in entering:duration-300",
        "exiting:animate-out exiting:fade-out exiting:duration-200"
      )}
    >
      <Modal
        className={cx(
          "w-full max-w-md rounded-2xl bg-white outline-none",
          "entering:animate-in entering:zoom-in-95 entering:duration-300",
          "exiting:animate-out exiting:zoom-out-95 exiting:duration-200",
          className
        )}
      >
        <Dialog aria-label={computedAriaLabel} className="outline-none">
          <div className={cx(contentClassName)}>
            {(title || isDismissable) && (
              <div className="flex items-center justify-between gap-4">
                {title ? (
                  <Heading
                    slot="title"
                    className="text-xl font-bold leading-7.5 text-secondary"
                  >
                    {title}
                  </Heading>
                ) : (
                  <span />
                )}

                {isDismissable && (
                  <button
                    type="button"
                    onClick={() => onOpenChange(false)}
                    aria-label="Close"
                    className={cx(
                      "inline-flex h-9 w-9 items-center justify-center rounded-lg",
                      "text-gray-400 hover:bg-gray-100 hover:text-gray-700",
                      "focus:outline-none focus:ring-2 focus:ring-gray-300"
                    )}
                  >
                    <X />
                  </button>
                )}
              </div>
            )}

            <div className="mt-4 space-y-4">{children}</div>

            {footer && (
              <div className="mt-6 w-full">
                <div className="flex gap-3">{footer}</div>
              </div>
            )}
          </div>
        </Dialog>
      </Modal>
    </ModalOverlay>
  );
}
