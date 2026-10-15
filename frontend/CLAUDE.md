# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요

**Findex**는 React 19 + TypeScript로 구축된 금융 지수 관리 대시보드입니다. 한국 금융 지수 데이터를 시각화하고, 지수 정보/데이터/외부 시스템 연동을 통합 관리하는 SPA 애플리케이션입니다.

## 필수 명령어 (Commands)

### 개발 환경 설정
```bash
# 의존성 설치
npm install

# 환경 변수 설정 (필수)
# .env 파일에 API_PROXY_TARGET 설정 필요
echo "API_PROXY_TARGET=http://localhost:8080" > .env

# 개발 서버 실행 (http://localhost:5173)
npm run dev

# 프로덕션 빌드 (타입 체크 포함)
npm run build

# 빌드된 결과물 미리보기
npm preview

# 린트 검사
npm run lint
```

### 중요 참고사항
- **환경 변수**: `.env` 파일에 `API_PROXY_TARGET` 필수 설정
- **API 프록시**: 개발 서버는 `/api` 경로를 `http://localhost:8080`으로 프록시 (vite.config.ts)
- **타입 체크**: 빌드 시 자동으로 TypeScript 타입 체크 수행 (`tsc -b`)

## 코드 아키텍처 (Architecture)

### 핵심 기술 스택
- **React 19.2.0** - 최신 React (JSX transform 사용)
- **TypeScript 5.9.3** - Strict mode 활성화
- **Vite 7.2.4** - 빌드 도구 및 개발 서버
- **React Router DOM 7.9.6** - 클라이언트 라우팅
- **Zustand 5.0.9** - 전역 상태 관리 (Redux 사용 안 함)
- **Tailwind CSS 4.1.17** - 유틸리티 기반 스타일링
- **React Aria Components 1.13.0** - 접근성 UI 프리미티브
- **Recharts 3.5.1** - 차트/데이터 시각화

### 라우팅 구조 (Routing)
```typescript
// src/main.tsx - React Router v7
/                     → /dashboard로 리다이렉트
/dashboard            → 대시보드 (주요 지수, 차트, 성과)
/index-management     → 지수 관리 (CRUD, Open API 동기화)
/data-management      → 데이터 관리 (CRUD, CSV 내보내기)
/integrations         → 연동 관리 (외부 시스템 동기화 모니터링)
```

### 상태 관리 패턴 (State Management)
**Zustand 스토어** (`/src/store/`)를 사용하며 일관된 패턴을 따름:

```typescript
// 스토어 구조 예시
interface Store {
  // 상태 (State)
  items: T[];
  isLoading: boolean;
  error: string | null;
  filters: FilterType;

  // 커서 기반 페이지네이션 (Cursor Pagination)
  hasNext: boolean;
  nextCursor: string | null;
  idAfter: number | null;

  // 액션 (Actions)
  fetch: () => Promise<void>;        // 첫 페이지 로드
  fetchNext: () => Promise<void>;    // 다음 페이지 로드 (무한 스크롤)
  setFilters: (filters) => void;     // 필터 업데이트
  reset: () => void;                 // 상태 초기화
}
```

**주요 스토어**:
- `modalStore.ts` - 전역 모달 관리
- `toastStore.ts` - 토스트 알림 관리
- `indexInfoListStore.ts` / `indexDataListStore.ts` - 커서 페이지네이션 기반 목록
- `indexChartStore.ts` - 차트 데이터
- `indexFavoriteStore.ts` - 즐겨찾기 관리
- `syncJobStore.ts` - 동기화 작업 추적

### API 통신 패턴 (API Communication)
중앙화된 axios 래퍼 (`/src/api/client.ts`)를 사용:

```typescript
// 모든 API 호출은 apiClient를 통해 수행
import apiClient from '@/api/client';

// 타입 안전한 메서드
apiClient.get<T>(url, params);
apiClient.post<T>(url, data);
apiClient.patch<T>(url, data);
apiClient.put<T>(url, data);
apiClient.delete<T>(url);
apiClient.multiPartPost<T>(url, formData);  // 파일 업로드용
apiClient.multiPartPatch<T>(url, formData);
```

**도메인별 API 파일**:
- `dashboardApi.ts` - 대시보드 엔드포인트
- `indexDataApi.ts` - 지수 데이터 CRUD
- `indexInfoApi.ts` - 지수 정보 CRUD
- `syncApi.ts` - 동기화 작업 엔드포인트

**중요**: 모든 API 응답은 TypeScript 인터페이스로 타입 정의됨 (`/src/model/`)

### 커서 기반 페이지네이션 (Cursor Pagination)
무한 스크롤(Infinite Scroll)을 위해 커서 페이지네이션 사용:

```typescript
// /src/model/pagination.ts
interface CursorPageResponse<T> {
  items: T[];
  hasNext: boolean;
  nextCursor: string | null;
  idAfter: number | null;
}
```

**구현 패턴**:
1. `use-infinite-scroll.ts` 훅으로 IntersectionObserver 구현
2. 스토어에서 `fetchNext()` 메서드로 다음 페이지 로드
3. `hasNext`가 `false`일 때 로딩 중단

### 컴포넌트 구조 (Component Structure)
```
src/components/
├── common/           # 재사용 가능한 공통 컴포넌트
│   ├── buttons/      # Button (20+ variants), DropdownButton
│   ├── modals/       # BaseModal, ConfirmModal, ModalContainer
│   ├── toast/        # ToastContainer, Toast
│   ├── table/        # Table 컴포넌트
│   ├── input/        # Input, Label, TextArea, HintText
│   ├── select/       # Select, MultiSelect, Combobox
│   ├── date-picker/  # React Aria 기반 DatePicker
│   ├── charts/       # Recharts 기반 차트 컴포넌트
│   └── ...
├── layout/           # Layout, Navigation (사이드바)
└── pages/            # 페이지별 특화 컴포넌트
    ├── dashboard/
    ├── index-management/
    ├── data-management/
    └── integrations/
```

**컴포넌트 패턴**:
- `common/` - 높은 재사용성, 상세한 prop 인터페이스
- `pages/` - 페이지 특화, `common/` 컴포넌트 조합
- `src/pages/` - 라우트 엔트리 포인트 (얇은 래퍼)

### 스타일링 패턴 (Styling)
```typescript
// tailwind-merge를 사용한 클래스 병합
import { cx } from '@/utils/cx';  // 또는 '@/cx'

// 조건부 클래스 병합
<div className={cx(
  'base-class',
  condition && 'conditional-class',
  variant === 'primary' && 'primary-class'
)} />

// sortCx 유틸리티 (스타일 객체 정리용)
import { sortCx } from '@/utils/cx';
```

**스타일 규칙**:
- Tailwind CSS 유틸리티 클래스 우선 사용
- 커스텀 테마는 `/src/styles/theme.css` (62KB)
- Pretendard 폰트 사용 (한글 타이포그래피)
- React Aria Components에 `tailwindcss-react-aria-components` 플러그인 적용

### 타입 정의 구조 (Type Definitions)
```
src/
├── model/           # 도메인 모델 (API 응답 타입)
│   ├── dashboard.ts
│   ├── indexData.ts
│   ├── indexInfo.ts
│   ├── pagination.ts
│   └── sync.ts
├── types/           # 공유 타입
│   ├── enums.ts     # Enum (SourceType, JobType, ResultType)
│   └── toast.ts
└── constants/       # 상수 정의 (select options, labels)
```

**중요**: TypeScript Strict Mode 활성화 - `any` 사용 지양

### 유틸리티 함수 (Utilities)
- `date.ts` - 한국어 날짜 포맷팅 (광범위한 날짜 처리)
- `cx.ts` - Tailwind 클래스 병합 (`tailwind-merge` 래퍼)
- `sort.ts` - 테이블 정렬 유틸리티
- `chart.ts` - 차트 데이터 변환
- `decimal.ts` - 숫자 포맷팅
- `download.ts` - 파일 다운로드 헬퍼

### 커스텀 훅 (Custom Hooks)
- `use-infinite-scroll.ts` - IntersectionObserver 기반 무한 스크롤
- `use-debounced-value.ts` - 디바운싱 유틸리티
- `use-breakpoint.ts` - 반응형 브레이크포인트 감지
- `use-resize-observer.ts` - 요소 크기 변화 감지

## 중요한 개발 규칙 (Development Guidelines)

### 코드 스타일
- **Prettier** - Import 자동 정렬 (react → 써드파티 → @/ → 상대경로)
- **ESLint** - React Hooks, TypeScript 규칙 적용
- **파일명**: kebab-case (예: `use-infinite-scroll.ts`)
- **컴포넌트명**: PascalCase (예: `DataManagement.tsx`)

### 주요 패턴 준수 사항
1. **API 호출**: 항상 `apiClient` 사용, 직접 axios 호출 금지
2. **전역 상태**: Zustand 스토어 사용, props drilling 최소화
3. **모달 관리**: `modalStore`를 통한 중앙 관리
4. **토스트 알림**: `toastStore`의 `addToast()` 사용
5. **스타일링**: `cx()` 유틸리티로 클래스 병합
6. **날짜 처리**: `utils/date.ts`의 함수 사용 (일관된 한국어 포맷)
7. **타입 안전성**: 모든 API 응답과 prop에 명시적 타입 정의

### 페이지별 주요 기능

#### Dashboard (`/dashboard`)
- 즐겨찾기 주요 지수 카드
- Recharts 기반 인터랙티브 지수 차트
- 지수 성과 순위
- 기간 필터 (일/주/월, 월/분기/년)

#### Index Management (`/index-management`)
- 지수 정보 CRUD
- 분류/이름/즐겨찾기 필터
- Open API 동기화
- 무한 스크롤 페이지네이션

#### Data Management (`/data-management`)
- 지수 데이터 CRUD
- 날짜 범위 필터
- CSV 내보내기
- Open API 데이터 동기화

#### Integrations (`/integrations`)
- 외부 시스템 동기화 상태 모니터링
- 동기화 이력 추적
- 성공/실패 메트릭
- 자동 동기화 설정

## Path Alias
```typescript
// vite.config.ts와 tsconfig.json에 설정됨
@ → src/

// 사용 예시
import apiClient from '@/api/client';
import { cx } from '@/utils/cx';
import Button from '@/components/common/buttons/Button';
```

## 주요 파일 경로 (Critical Files)
```
src/main.tsx                    # 앱 엔트리 포인트, 라우터 설정
src/api/client.ts               # API 클라이언트 래퍼
src/components/layout/Layout.tsx # 공통 레이아웃 구조
vite.config.ts                  # Vite 설정 (프록시, alias)
src/store/modalStore.ts         # 전역 모달 관리
src/store/toastStore.ts         # 전역 토스트 관리
```

## Git Workflow
- **현재 브랜치**: `2.0.x`
- **커밋 메시지**: 한국어 작성
- **GitHub 템플릿**:
  - Issue: `.github/ISSUE_TEMPLATE/` (feature, refactor, fix)
  - PR: `.github/pull_request_template.md`

## 테스트
**현재 테스트 프레임워크 없음** - 테스트가 필요한 경우 Jest/Vitest 설정 필요

## 배포
- **Vercel** 배포 설정 (`vercel.json`)
- SPA 라우팅을 위한 rewrite 규칙 포함
