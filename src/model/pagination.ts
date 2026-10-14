export interface CursorPageResponse<T> {
  content: T[];
  nextCursor: string;
  nextIdAfter: number;
  size: number;
  totalElements: number;
  hasNext: boolean;
}
