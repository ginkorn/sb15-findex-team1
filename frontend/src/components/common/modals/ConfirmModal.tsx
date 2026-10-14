import React from "react";
import { RefreshCcw05, Trash01 } from "@untitledui/icons";
import { Button } from "../buttons/Button";
import { BaseModal } from "./BaseModal";

interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  children: React.ReactNode;
  variant?: "danger" | "primary";
  confirmText?: string;
  isLoading?: boolean;
  isDisabled?: boolean;
  isSingleButton?: boolean;
}

const ConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  children,
  variant = "danger",
  confirmText,
  isLoading = false,
  isDisabled = false,
  isSingleButton = false,
}: ConfirmModalProps) => {
  // 상태별 스타일 설정
  const config = {
    danger: {
      buttonColor: "primary-destructive" as const,
      icon: Trash01,
      iconClass: "text-fg-error-secondary",
      bgClass: "bg-error-secondary",
      defaultText: "삭제",
    },
    primary: {
      buttonColor: "primary" as const,
      icon: RefreshCcw05,
      iconClass: "text-brand-600",
      bgClass: "bg-brand-secondary",
      defaultText: "연동",
    },
  }[variant];
  const Icon = config.icon;

  return (
    <BaseModal
      isOpen={isOpen}
      onOpenChange={onClose}
      className="w-[376px]"
      contentClassName="pt-4 px-5 pb-6"
    >
      <div className="-mt-12 flex w-[328px] flex-col justify-start pb-4">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-full ${config.bgClass}`}
        >
          <Icon className={`${config.iconClass} h-6 w-6`} />
        </div>
        <div className="pt-5 pb-1 text-xl font-semibold">{title}</div>
        <div>{children}</div>
      </div>
      <div className="flex justify-between gap-3">
        {!isSingleButton && (
          <Button
            color="secondary"
            className="w-full"
            onClick={onClose}
            isDisabled={isLoading}
          >
            취소
          </Button>
        )}
        <Button
          type="submit"
          color={config.buttonColor}
          className="w-full"
          onClick={onConfirm}
          isDisabled={isDisabled || isLoading}
          isLoading={isLoading}
          showTextWhileLoading
        >
          {confirmText || config.defaultText}
        </Button>
      </div>
    </BaseModal>
  );
};

export default ConfirmModal;
