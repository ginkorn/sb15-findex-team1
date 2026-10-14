const SIGNED_DECIMAL_REGEX = /^-?\d+(\.\d+)?$/;

export function isValidDecimal(value: string) {
  const v = value.trim();
  return v.length > 0 && SIGNED_DECIMAL_REGEX.test(v);
}
