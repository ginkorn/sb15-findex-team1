import { Plus, RefreshCcw05, Share01 } from "@untitledui/icons";
import { Button } from "@/components/common/buttons/Button";
import type { Index } from "@/pages/DataManagement";
import { useIndexDataListStore } from "@/store/indexDataListStore";
import { useModalStore } from "@/store/modalStore";
import { useToastStore } from "@/store/toastStore";

interface DataManagementHeaderProps {
  index: Index | null;
}

export default function DataManagementHeader({
  index,
}: DataManagementHeaderProps) {
  const { totalElements, isLoadingExport, exportData } =
    useIndexDataListStore();
  const { successToast, errorToast } = useToastStore();
  const { openIndexDataForm, openIndexDataSync } = useModalStore();

  const handleExportClick = async () => {
    try {
      await exportData();
      successToast("성공적으로 다운로드되었습니다.");
    } catch (error) {
      errorToast("다운로드에 실패하였습니다.");
    }
  };

  const handleAddClick = () => {
    openIndexDataForm({
      mode: "create",
    });
  };

  const handleSyncApiClick = () => {
    if (!index) {
      errorToast("연동할 지수를 선택해주세요.");
      return;
    }
    openIndexDataSync({
      index: index,
    });
  };

  return (
    <div className="border-secondary flex items-center justify-between border-b px-6 py-5">
      <div className="flex h-full shrink-0 flex-col justify-center max-sm:justify-start">
        <h2 className="text-lg leading-7 font-semibold">데이터 목록</h2>
        <span className="text-tertiary text-sm font-normal">
          총 {totalElements.toLocaleString("ko-KR")}개
        </span>
      </div>
      <div className="flex items-center gap-3 max-sm:flex-col max-sm:items-end">
        <Button
          color="tertiary"
          iconLeading={<Share01 size={20} />}
          onClick={handleExportClick}
          disabled={isLoadingExport}
        >
          Export
        </Button>
        <Button
          color="secondary"
          iconLeading={<Plus size={20} />}
          onClick={handleAddClick}
        >
          데이터 등록
        </Button>
        <Button
          iconLeading={<RefreshCcw05 size={20} />}
          onClick={handleSyncApiClick}
        >
          Open API 연동
        </Button>
      </div>
    </div>
  );
}
