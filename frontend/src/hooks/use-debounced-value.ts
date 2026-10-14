import { useEffect, useState } from "react";

// 디바운스 훅
export function useDebouncedValue<T>(value: T, delay = 300) {
  const [debounced, setDebounce] = useState(value);

  useEffect(() => {
    const t = setTimeout(() => setDebounce(value), delay);
    return () => clearTimeout(t);
  }, [delay, value]);

  return debounced;
}
