import { describe, it, expect } from "vitest";
import type { Salary } from "~/api/client";
import { getYears, getLatestYear } from "./salary";

const salaries = [
  { targetDate: "2024-01", amount: 300000 },
  { targetDate: "2025-03", amount: 300000 },
  { targetDate: "2026-05", amount: 300000 },
  { targetDate: "2025-07", amount: 300000 },
  { targetDate: "2024-09", amount: 300000 },
];

describe("getYears", () => {
  it("重複・昇順", () => {
    const result = getYears(salaries as unknown as Salary[]);
    expect(result).toEqual(["2024", "2025", "2026"]);
  });

  it("空配列", () => {
    const result = getYears([]);
    expect(result).toEqual([]);
  });
});

describe("getYear", () => {
  it("重複", () => {
    const result = getLatestYear(salaries as unknown as Salary[]);
    expect(result).toEqual("2026");
  });

  it("空配列", () => {
    const result = getLatestYear([]);
    expect(result).toEqual(undefined);
  });
});
