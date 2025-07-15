// trimArray.test.ts
import { describe, it, expect } from "vitest";
import { trimArray, chunkArray } from "./array";

describe("trimArray", () => {
  it("should return the same array when size is under the maxSize", () => {
    const result = trimArray([1, 2], 5);
    expect(result).toEqual([1, 2]);
  });

  it("should trim from the start by default when over maxSize", () => {
    const result = trimArray([1, 2, 3, 4, 5], 3);
    expect(result).toEqual([1, 2, 3]);
  });

  it("should trim from the start explicitly", () => {
    const result = trimArray([1, 2, 3, 4, 5], 2, { from: "start" });
    expect(result).toEqual([1, 2]);
  });

  it("should trim from the end when specified", () => {
    const result = trimArray([1, 2, 3, 4, 5], 2, { from: "end" });
    expect(result).toEqual([4, 5]);
  });

  it("should return an empty array when maxSize is 0", () => {
    const result = trimArray([1, 2, 3], 0);
    expect(result).toEqual([]);
  });

  it("should handle empty array input", () => {
    const result = trimArray([], 3);
    expect(result).toEqual([]);
  });
});

describe("chunkArray", () => {
  it("splits array into chunks of given size: 1", () => {
    expect(chunkArray([1, 2, 3, 4], 1)).toEqual([[1], [2], [3], [4]]);
  });

  it("splits array into chunks of given size] 2", () => {
    expect(chunkArray([1, 2, 3, 4, 5, 6], 2)).toEqual([
      [1, 2],
      [3, 4],
      [5, 6],
    ]);
  });

  it("handles arrays that do not divide evenly", () => {
    expect(chunkArray([1, 2, 3, 4, 5], 2)).toEqual([[1, 2], [3, 4], [5]]);
  });

  it("returns empty array when input is empty", () => {
    expect(chunkArray([], 3)).toEqual([]);
  });

  it("returns single chunk when array length is less than chunk size", () => {
    expect(chunkArray([1, 2], 5)).toEqual([[1, 2]]);
  });

  it("throws error or behaves as expected when size is 0 or negative", () => {
    expect(chunkArray([1, 2, 3], 0)).toEqual([]); // 無限ループ回避のために拡張検討余地あり
    expect(chunkArray([1, 2, 3], -1)).toEqual([]); // 境界条件に応じて処理調整可能
  });
});
