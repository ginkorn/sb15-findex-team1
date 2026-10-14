import type { ReactNode } from "react";
import { create } from "zustand";
import type { IndexDataDto } from "@/model/indexData";
import type { IndexInfoResponse } from "@/model/indexInfo";
import type { Index } from "@/pages/DataManagement";

type ModalState =
  | { type: "confirm"; props: ConfirmProps }
  | { type: "indexDataForm"; props: IndexDataModalProps }
  | { type: "indexDataSync"; props: IndexDataSyncModalProps }
  | { type: "indexForm"; props: IndexModalProps }
  | { type: "indexSync" };

// Confirm
interface ConfirmProps {
  title: string;
  variant?: "danger" | "primary";
  description: ReactNode;
  onConfirm: () => void | Promise<void>;
}

// IndexData 지수데이터
interface IndexDataModalProps {
  mode: "create" | "edit" | "view";
  initial?: Partial<IndexDataDto>;
}

// IndexDataSync 지수데이터 연동
interface IndexDataSyncModalProps {
  index: Index;
}

// IndexInfo 지수정보
interface IndexModalProps {
  mode: "create" | "edit" | "view";
  initial?: IndexInfoResponse;
}

interface ModalStore {
  modal: ModalState | null;

  close: () => void;

  openConfirm: (props: ConfirmProps) => void;
  openIndexDataForm: (props: IndexDataModalProps) => void;
  openIndexDataSync: (props: IndexDataSyncModalProps) => void;
  openIndexForm: (props: IndexModalProps) => void;
  openIndexSync: () => void;
}

export const useModalStore = create<ModalStore>((set) => ({
  modal: null,

  close: () => set({ modal: null }),

  openConfirm: (props) => set({ modal: { type: "confirm", props } }),
  openIndexDataForm: (props) =>
    set({ modal: { type: "indexDataForm", props } }),
  openIndexDataSync: (props) =>
    set({ modal: { type: "indexDataSync", props } }),
  openIndexForm: (props) => set({ modal: { type: "indexForm", props } }),
  openIndexSync: () => set({ modal: { type: "indexSync" } }),
}));
