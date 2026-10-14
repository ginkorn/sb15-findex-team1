# 🧩 Button 컴포넌트

공통적으로 사용 가능한 **재사용 버튼 UI 컴포넌트**입니다.\
Untitled UI + React Aria 스타일 구조 기반으로 다양한 상태를 시각적으로 표현할 수 있습니다.

---

## 🧑🏻‍💻 사용법

```tsx
import { Button } from "@/components/common/buttons/Button";

// 기본 버튼
<Button color="primary">등록</Button>

// 비활성화
<Button color="primary" isDisabled>
  저장
</Button>

// 로딩 상태
<Button
  isLoading
  showTextWhileLoading
>
  처리 중...
</Button>

// 아이콘 포함
import { Plus } from "@untitledui/icons";

<Button
  color="primary"
  iconLeading={<Plus data-icon />}
>
  직원 등록하기
</Button>

// 클릭 핸들러 포함
const [loading, setLoading] = useState(false);

const handleClick = () => {
  setLoading(true);
  setTimeout(() => setLoading(false), 1000);
};

<Button
  color="primary"
  isLoading={loading}
  onClick={handleClick}
>
  {loading ? "등록 중..." : "등록하기"}
</Button>
```

---

## ✨ Props

| 이름                   | 타입                                                                    | 필수 | 기본값      | 설명                     |
| ---------------------- | ----------------------------------------------------------------------- | ---- | ----------- | ------------------------ |
| `color`                | `"primary"` \| `"secondary"` \| `"primary-destructive"` \| `"tertiary"` | ❌   | `"primary"` | 버튼 종류 및 색상 스타일 |
| `size`                 | `keyof typeof styles.sizes` (`"common"`)                                | ❌   | `"common"`  | 버튼 크기 스타일         |
| `isDisabled`           | `boolean`                                                               | ❌   | `false`     | 버튼 비활성화 여부       |
| `isLoading`            | `boolean`                                                               | ❌   | `false`     | 로딩 상태 표시           |
| `showTextWhileLoading` | `boolean`                                                               | ❌   | `false`     | 로딩 시 텍스트 계속 표시 |
| `iconLeading`          | `ReactNode`                                                             | ❌   | -           | 텍스트 앞에 아이콘 표시  |
| `noTextPadding`        | `boolean`                                                               | ❌   | `false`     | 텍스트 좌우 padding 제거 |
| `onClick`              | `() => void`                                                            | ❌   | -           | 버튼 클릭 이벤트         |
| `children`             | `ReactNode`                                                             | ❌   | -           | 버튼 표시 텍스트         |

---

## 🎨 스타일 가이드 (기본 Tailwind 기반)

| 값                    | 설명                      |
| --------------------- | ------------------------- |
| `primary`             | 브랜드 메인 버튼 (기본값) |
| `secondary`           | 보조 버튼                 |
| `primary-destructive` | 위험/삭제 액션 버튼       |
| `tertiary`            | 테두리 없는 텍스트형 버튼 |

---

## 🧱 Layout & Icon 규칙

| 형태             | render 조건                                 |
| ---------------- | ------------------------------------------- |
| 아이콘Only 버튼  | `iconLeading` 제공 + `children` 없을 때     |
| 로딩 상태 표시   | 아이콘 대신 Spinner 표시 (`isLoading=true`) |
| 텍스트 유지 로딩 | `showTextWhileLoading=true` 설정 시 적용    |

---

### 🧩 참고 사항

- ...otherProps를 통해 HTML 속성 자동 전달 → onClick 포함
