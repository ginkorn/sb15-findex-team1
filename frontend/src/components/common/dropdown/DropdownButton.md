# 🧩 DropdownButton 컴포넌트

선택형 입력이 필요한 곳에서 공통으로 사용 가능한 재사용 드롭다운 UI 컴포넌트입니다.
Untitled UI + React Aria 기반 Dropdown 컴포넌트를 래핑해서, 상태 선택/필터 UI 등을 간단하게 만들 수 있습니다.

---

## 🧑🏻‍💻 사용법

```tsx
// enums.ts에 데이터 추가
export const EmploymentState = {
  ALL: "ALL",
  ACTIVE: "ACTIVE",
  ON_LEAVE: "ON_LEAVE",
  RESIGNED: "RESIGNED",
} as const;

export type EmploymentStateType =
  (typeof EmploymentState)[keyof typeof EmploymentState];

// EmploymentStateLabels.ts에 라벨 추가
import { EmploymentState, type EmploymentStateType } from "@/types/enums";

export const EmploymentStateLabels: Record<EmploymentStateType, string> = {
  [EmploymentState.ALL]: "전체",
  [EmploymentState.ACTIVE]: "재직중",
  [EmploymentState.ON_LEAVE]: "휴직중",
  [EmploymentState.RESIGNED]: "퇴사",
};

// 페이지에서 import
import { useState } from "react";
import { DropdownButton } from "@/components/common/dropdown/DropdownButton";
import { StateLabels } from "@/constants/statusLabels";

const Example = () => {
  const [status, setStatus] = useState<string | null>(null);

  return (
    <>
      <DropdownButton
        placeholder="상태"
        label={EmploymentStateLabels}
        onChange={(value) => {
          setStatusFilter(value);
        }}
      />
    </>
  );
};
```

---

## ✨ Props

| 이름          | 타입                      | 필수 | 기본값      | 설명                                                                |
| ------------- | ------------------------- | ---- | ----------- | ------------------------------------------------------------------- |
| `placeholder` | `string`                  | ❌   | `undefined` | 선택 전 버튼에 보여줄 기본 텍스트. (선택 후에는 해당 라벨로 교체됨) |
| `label`       | `Record<string, string>`  | ✅   | -           | 드롭다운 항목 목록. key는 내부값, value는 화면에 표시될 텍스트      |
| `onChange`    | `(value: string) => void` | ❌   | `undefined` | 항목 선택 시 호출되는 콜백. 선택된 key를 인자로 전달                |

---

## 🎨 UI 스타일 가이드

- placeholder

아무 것도 선택되지 않았을 때 버튼에 표시되는 기본 텍스트

- label

Record<string, string> 형태로, 키(id) → 표시용 텍스트를 매핑

- 드롭다운 항목 클릭 시

내부적으로 selectedKey를 업데이트해서 버튼 텍스트를 선택된 라벨로 변경, 선택된 항목 오른쪽에 ✅ 아이콘 표시

onChange 콜백으로 선택된 key를 상위 컴포넌트에 전달

---

## 🧱 Layout & Icon 규칙

| 형태             | render 조건                                 |
| ---------------- | ------------------------------------------- |
| 아이콘Only 버튼  | `iconLeading` 제공 + `children` 없을 때     |
| 로딩 상태 표시   | 아이콘 대신 Spinner 표시 (`isLoading=true`) |
| 텍스트 유지 로딩 | `showTextWhileLoading=true` 설정 시 적용    |

---

### 🧩 참고 사항

- DropdownButton은 UI + 간단한 내부 상태 관리까지만 담당하고,
  실제 비즈니스 로직(필터링, API 요청 등)은 onChange를 통해 상위에서 처리하는 구조를 권장합니다.

- label은 순서 보장이 중요하다면 Record 대신
  Array<{ id: string; label: string }> 형태로 바꾸는 것도 고려할 수 있습니다. (현재 구현은 Record 기준)
