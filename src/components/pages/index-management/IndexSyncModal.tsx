import { useState } from "react";
import { syncIndexInfo } from "@/api/indexInfoApi";
import { Dot } from "@/components/common/foundations/dot-icon";
import ConfirmModal from "@/components/common/modals/ConfirmModal";
import { useIndexIndexListStore } from "@/store/indexInfoListStore";
import { useModalStore } from "@/store/modalStore";
import { useToastStore } from "@/store/toastStore";

const IndexSyncModal = () => {
  const { close } = useModalStore();
  const { fetch } = useIndexIndexListStore();
  const { successToast, errorToast } = useToastStore();

  // 연동 컴펌 모달 -> 연동 결과 모달
  const [step, setStep] = useState<"confirm" | "result">("confirm");
  // 연동중(로딩) 상태
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState({
    successCount: 0,
    failCount: 0,
  });

  const handleSync = async () => {
    setIsSyncing(true);
    try {
      const { successCount, failCount } = await syncIndexInfo();
      setSyncResult({ successCount, failCount });
      fetch();
      successToast("연동이 완료되었습니다.");
      setStep("result");
    } catch (err) {
      errorToast("지수 연동에 실패하였습니다.");
      close();
    } finally {
      setIsSyncing(false);
    }
  };
  return (
    <>
      {step === "confirm" ? (
        <ConfirmModal
          isOpen={true}
          onClose={close}
          onConfirm={handleSync}
          title="지수 정보 연동"
          variant="primary"
          isLoading={isSyncing}
          confirmText={isSyncing ? "연동 중" : "연동"}
        >
          <p className="text-sm text-gray-500">
            Open API를 통해 최신 정보를 연동합니다.
          </p>
        </ConfirmModal>
      ) : (
        <ConfirmModal
          isOpen={true}
          onClose={() => {
            close();
            setStep("confirm");
          }}
          onConfirm={close}
          title="지수 정보 연동"
          variant="primary"
          isSingleButton
          confirmText="확인"
        >
          <div className="text-text-tertiary flex flex-col text-sm">
            <div className="flex items-center justify-start gap-1">
              <Dot className="text-fg-success-secondary h-1.5 w-1.5" />
              <span>성공 {syncResult.successCount}건</span>
            </div>
            <div className="flex items-center justify-start gap-1">
              <Dot className="text-fg-error-primary h-1.5 w-1.5" />
              <span>실패 {syncResult.failCount}건</span>
            </div>
          </div>
        </ConfirmModal>
      )}
    </>
  );
};

export default IndexSyncModal;
