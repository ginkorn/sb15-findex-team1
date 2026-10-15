import { useEffect, useRef } from "react";

interface UseInfiniteScrollOptions {
  hasNext: boolean;
  isLoading: boolean;
  onLoadMore: () => void | Promise<void>;
  rootRef?: React.RefObject<HTMLElement | null>;
  rootMargin?: string;
  threshold?: number;
}

/**
 * IntersectionObserver 기반 무한 스크롤 훅
 * - 마지막 sentinel 요소에 ref만 달아주면, 교차 시 onLoadMore 호출
 */
export function useInfiniteScroll({
  hasNext,
  isLoading,
  onLoadMore,
  rootRef,
  rootMargin = "0px 0px 200px 0px",
  threshold = 0,
}: UseInfiniteScrollOptions) {
  const loadMoreRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!hasNext) return;
    if (isLoading) return;

    const root = rootRef?.current ?? null;
    const target = loadMoreRef.current;
    if (!target) return;

    const observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting) {
          void onLoadMore();
        }
      },
      {
        root,
        rootMargin,
        threshold,
      }
    );

    observer.observe(target);
    return () => observer.disconnect();
  }, [hasNext, isLoading, onLoadMore, rootMargin, threshold, rootRef]);

  return { loadMoreRef };
}
