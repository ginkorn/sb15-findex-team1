import { useState } from "react";
import { Form } from "react-aria-components";
import { createIndexInfo, updateIndexInfo } from "@/api/indexInfoApi";
import { Button } from "@/components/common/buttons/Button";
import { DatePicker } from "@/components/common/date-picker/DatePicker";
import { Input } from "@/components/common/input/Input";
import { Label } from "@/components/common/input/Label";
import { BaseModal } from "@/components/common/modals/BaseModal";
import { Toggle } from "@/components/common/toggle/toggle";
import type { IndexInfoRequest, IndexInfoResponse } from "@/model/indexInfo";
import { useIndexIndexListStore } from "@/store/indexInfoListStore";
import { useToastStore } from "@/store/toastStore";
import { parseDateValue } from "@/utils/date";

interface IndexModalProps {
  isOpen: boolean;
  onClose: () => void;
  mode: "create" | "edit" | "view";
  initial?: IndexInfoResponse;
}
const IndexModal = ({ isOpen, onClose, mode, initial }: IndexModalProps) => {
  const { fetch } = useIndexIndexListStore();
  // 성공, 에러 토스트
  const { successToast, errorToast } = useToastStore();
  // 로딩 상태
  const [isSubmitLoading, setIsSubmitLoading] = useState(false);
  // 상세, 수정, 생성 모드
  const isView = mode === "view";
  const isEdit = mode === "edit";
  const isCreate = mode === "create";
  // 폼 상태 관리
  const [formData, setFormData] = useState(() => {
    return {
      indexClassification: initial?.indexClassification || "",
      indexName: initial?.indexName || "",
      basePointInTime: parseDateValue(initial?.basePointInTime) || null,
      employedItemsCount: initial?.employedItemsCount ?? null,
      baseIndex: initial?.baseIndex ?? null,
      favorite: initial?.favorite ?? true,
    };
  });

  // 입력값 변경 핸들러
  const handleChange = (key: keyof IndexInfoRequest, value: unknown) => {
    if (key === "employedItemsCount" || key === "baseIndex") {
      const onlyNumber = String(value).replace(/\D/g, "");

      const numValue = Number(onlyNumber);

      setFormData((prev) => ({ ...prev, [key]: numValue }));
      return;
    }
    setFormData((prev) => ({ ...prev, [key]: value }));
  };

  // 등록 수정 핸들러
  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    if (isView) {
      onClose();
      return;
    }
    setIsSubmitLoading(true);
    try {
      const payload: IndexInfoRequest = {
        indexClassification: formData.indexClassification,
        indexName: formData.indexName,
        employedItemsCount: Number(formData.employedItemsCount),
        baseIndex: Number(formData.baseIndex),
        basePointInTime: formData.basePointInTime?.toString() || "",
        favorite: formData.favorite,
      };

      if (isEdit && initial?.id) {
        await updateIndexInfo(initial.id, payload);
        successToast("성공적으로 수정되었습니다.");
      } else {
        await createIndexInfo(payload);
        successToast("성공적으로 등록되었습니다.");
      }

      fetch();
      onClose();
    } catch (err) {
      errorToast(isEdit ? "수정에 실패했습니다." : "등록에 실패하였습니다.");
    } finally {
      setIsSubmitLoading(false);
    }
  };

  // 입력값 조건
  const isFormValid =
    !!formData.indexClassification &&
    !!formData.indexName &&
    !!formData.basePointInTime &&
    formData.employedItemsCount !== null &&
    formData.employedItemsCount !== 0 &&
    formData.baseIndex !== null &&
    formData.baseIndex !== 0;

  // 타이틀 분기 처리
  const getTitle = () => {
    if (isView || isEdit) return "지수 정보 수정";
    return "지수 정보 등록";
  };

  // 기준시점 선택 취소 핸들러
  const handleCancel = () => {
    setFormData((prev) => ({
      ...prev,
      basePointInTime: parseDateValue(initial?.basePointInTime) || null,
    }));
  };
  return (
    <>
      <BaseModal
        isOpen={isOpen}
        onOpenChange={(open) => !open && onClose()}
        title={getTitle()}
        className="w-[376px]"
        contentClassName="p-6"
        footer={
          <div className="flex w-full justify-between gap-3">
            {!isView && (
              <Button
                color="secondary"
                onClick={onClose}
                className="w-full"
                isDisabled={isSubmitLoading}
              >
                취소
              </Button>
            )}
            <Button
              onClick={handleSubmit}
              className="w-full"
              isDisabled={(!isView && !isFormValid) || isSubmitLoading}
              isLoading={isSubmitLoading}
              showTextWhileLoading
            >
              {isView || isEdit ? "수정" : "등록"}
            </Button>
          </div>
        }
      >
        <Form validationBehavior="aria" onSubmit={handleSubmit}>
          <div className="flex w-full flex-col gap-4">
            <Input
              label="분류명"
              placeholder="분류명을 입력해주세요"
              value={formData.indexClassification}
              onChange={(val) => handleChange("indexClassification", val)}
              isRequired
              isDisabled={!isCreate}
            />
            <Input
              label="지수명"
              placeholder="지수명을 입력해주세요"
              value={formData.indexName}
              onChange={(val) => handleChange("indexName", val)}
              isRequired
              isDisabled={!isCreate}
            />
            <div className="flex flex-col gap-1.5">
              <Label id="hire-date-label" isRequired>
                기준 시점
              </Label>
              <DatePicker
                value={formData.basePointInTime}
                placeholder="날짜를 선택해주세요"
                defaultValue={formData.basePointInTime ?? undefined}
                onChange={(value) => handleChange("basePointInTime", value)}
                onCancel={handleCancel}
              />
            </div>
            <div className="flex justify-between gap-3">
              <Input
                inputMode="decimal"
                pattern="[0-9]*"
                label="채용 종목 수"
                placeholder="0"
                value={
                  formData.employedItemsCount === null
                    ? undefined
                    : String(formData.employedItemsCount)
                }
                onChange={(val) => handleChange("employedItemsCount", val)}
                isRequired
                isInvalid={!isView && formData.employedItemsCount === 0}
              />
              <Input
                inputMode="decimal"
                pattern="[0-9]*"
                label="기준 지수"
                placeholder="0"
                value={
                  formData.baseIndex === null
                    ? undefined
                    : String(formData.baseIndex)
                }
                onChange={(val) => handleChange("baseIndex", val)}
                isRequired
                isInvalid={!isView && formData.baseIndex === 0}
              />
            </div>
          </div>
          <Toggle
            className="pt-8"
            label="즐겨찾기 등록"
            isSelected={formData.favorite}
            onChange={(value) => handleChange("favorite", value)}
          />
        </Form>
      </BaseModal>
    </>
  );
};

export default IndexModal;
