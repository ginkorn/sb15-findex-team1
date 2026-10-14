import { Plus, RefreshCcw05 } from "@untitledui/icons";
import { Button } from "@/components/common/buttons/Button";
import { useIndexIndexListStore } from "@/store/indexInfoListStore";
import { useModalStore } from "@/store/modalStore";

export const IndexHeader = () => {
  // 지수 개수
  const { totalElements } = useIndexIndexListStore();
  // 지수 연동, 지수 등록 모달
  const { openIndexSync, openIndexForm } = useModalStore();

  // 지수 등록 버튼 클릭 핸들러
  const handleCreateClick = () => {
    openIndexForm({
      mode: "create",
    });
  };

  // 지수 연동 버튼 클릭 핸들러
  const handleSyncClick = () => {
    openIndexSync();
  };
  return (
    <div className="border-secondary flex justify-between border-b px-6 py-5">
      <div className="flex flex-col gap-0.5">
        <p className="text-lg font-semibold">지수 목록</p>
        <p className="text-tertiary text-sm">총 {totalElements}개</p>
      </div>
      <div className="flex gap-3">
        <Button
          iconLeading={<Plus size={20} stroke="#414651" />}
          showTextWhileLoading
          color="secondary"
          onClick={handleCreateClick}
        >
          지수 등록
        </Button>
        <Button
          iconLeading={<RefreshCcw05 size={20} stroke="white" />}
          showTextWhileLoading
          onClick={handleSyncClick}
        >
          Open API 연동
        </Button>
      </div>
    </div>
  );
};

export default IndexHeader;
