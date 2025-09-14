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

export function chunkArray<T>(array: T[], size: number): T[][] {
  const result: T[][] = [];
  if (size <= 0) {
    return result;
  }
  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size));
  }
  return result;
}

export function padArray<T>(
  array: T[],
  targetLength: number,
  padValue: T
): T[] {
  if (array.length >= targetLength) {
    return array.slice();
  }
  const paddedArray = array.slice();
  while (paddedArray.length < targetLength) {
    paddedArray.push(padValue);
  }
  return paddedArray;
}
