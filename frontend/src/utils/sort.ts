// src/utils/sort.ts
import type { SortDescriptor } from "react-aria-components";

/**
 * react-aria SortDescriptor 기반 공통 정렬 함수
 * - number면 숫자 비교
 * - 그 외는 문자열로 비교
 */
export function sortByDescriptor<T extends object>(
  items: T[],
  sortDescriptor: SortDescriptor,
): T[] {
  const { column, direction } = sortDescriptor;

  if (!column) return items;
  if (!items.length) return items;

  const copied = [...items];

  copied.sort((a, b) => {
    const first = a[column as keyof T];
    const second = b[column as keyof T];

    if (first == null || second == null) return 0;

    // 숫자 정렬
    if (typeof first === "number" && typeof second === "number") {
      return direction === "descending" ? second - first : first - second;
    }

    // 나머지는 문자열 정렬
    const f = String(first);
    const s = String(second);
    let cmp = f.localeCompare(s);

    if (direction === "descending") {
      cmp *= -1;
    }

    return cmp;
  });

  return copied;
}

export const isActiveSortColumn = (
  columnName: string,
  sortDescriptor: SortDescriptor,
) => sortDescriptor.column === columnName;
