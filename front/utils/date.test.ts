import { describe, it, expect } from "vitest";
import { getShortMonthLabel } from "./date";

describe("getShortMonthLabel", () => {
  it("1〜12月を英語の短縮表記にする", () => {
    const result = Array.from({ length: 12 }, (_, i) =>
      getShortMonthLabel(i + 1),
    );
    expect(result).toEqual([
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec",
    ]);
  });

  it("範囲外の月は空文字を返す", () => {
    expect(getShortMonthLabel(0)).toBe("");
    expect(getShortMonthLabel(13)).toBe("");
    expect(getShortMonthLabel(-1)).toBe("");
  });

  it("整数以外は空文字を返す", () => {
    expect(getShortMonthLabel(1.5)).toBe("");
    expect(getShortMonthLabel(NaN)).toBe("");
  });
});
