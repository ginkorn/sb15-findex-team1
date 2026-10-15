"use client";

import IndexDataModal from "@/components/pages/data-management/IndexDataModal";
import IndexDataSyncModal from "@/components/pages/data-management/IndexDataSyncModal";
import IndexModal from "@/components/pages/index-management/IndexModal";
import IndexSyncModal from "@/components/pages/index-management/IndexSyncModal";
import { useModalStore } from "@/store/modalStore";
import ConfirmModal from "./ConfirmModal";

export function ModalContainer() {
  const { modal, close } = useModalStore();

  if (!modal) return null;

  switch (modal.type) {
    case "confirm":
      return (
        <ConfirmModal
          isOpen
          title={modal.props.title}
          variant={modal.props.variant}
          onClose={close}
          onConfirm={async () => {
            await modal.props.onConfirm();
            close();
          }}
        >
          {modal.props.description}
        </ConfirmModal>
      );

    case "indexDataForm":
      return (
        <IndexDataModal
          isOpen
          onClose={close}
          mode={modal.props.mode}
          initial={modal.props.initial}
        />
      );

    case "indexDataSync":
      return (
        <IndexDataSyncModal isOpen onClose={close} index={modal.props.index} />
      );
    case "indexForm":
      return (
        <IndexModal
          isOpen
          onClose={close}
          mode={modal.props.mode}
          initial={modal.props.initial}
        />
      );
    case "indexSync":
      return <IndexSyncModal />;
    default:
      return null;
  }
}
