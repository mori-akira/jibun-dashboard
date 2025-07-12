// trimArray.test.ts
import { describe, it, expect } from "vitest";
import { trimArray } from "./trim-array";

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
