export function trimArray<T>(
  arr: T[],
  maxSize: number,
  options?: { from?: "start" | "end" }
): T[] {
  const direction = options?.from ?? "start";
  if (arr.length <= maxSize) {
    return arr;
  }
  if (direction === "start") {
    return arr.slice(0, maxSize);
  } else {
    return arr.slice(arr.length - maxSize);
  }
}
