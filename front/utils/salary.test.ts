import { describe, it, expect } from "vitest";
import type { Salary } from "~/api/client";
import {
  getMonthsInFinancialYear,
  getFinancialYear,
  getFinancialYears,
  filterSalaryByFinancialYear,
  aggregateAnnually,
} from "./salary";

const salaries: Salary[] = [
  { targetDate: "2024-04-01", overview: { grossIncome: 100000 } } as Salary,
  { targetDate: "2024-03-01", overview: { grossIncome: 200000 } } as Salary,
  { targetDate: "2026-04-01", overview: { grossIncome: 300000 } } as Salary,
  { targetDate: "2027-03-01", overview: { grossIncome: 400000 } } as Salary,
  { targetDate: "2024-09-01", overview: { grossIncome: 500000 } } as Salary,
  { targetDate: "2024-08-01", overview: { grossIncome: 600000 } } as Salary,
  { targetDate: "2024-10-01", overview: { grossIncome: 700000 } } as Salary,
];

describe("getMonthsInFinancialYear", () => {
  it("正常", () => {
    const result = getMonthsInFinancialYear("2024", 4);
    expect(result).toEqual([
      "2024-04",
      "2024-05",
      "2024-06",
      "2024-07",
      "2024-08",
      "2024-09",
      "2024-10",
      "2024-11",
      "2024-12",
      "2025-01",
      "2025-02",
      "2025-03",
    ]);
  });
});

describe("getFinancialYear", () => {
  it("境界", () => {
    let result = getFinancialYear({ targetDate: "2024-03-01" } as Salary, 4);
    expect(result).toEqual("2023");
    result = getFinancialYear({ targetDate: "2024-04-01" } as Salary, 4);
    expect(result).toEqual("2024");
  });

  it("空文字", () => {
    const result = getFinancialYear({ targetDate: "" } as Salary, 4);
    expect(result).toEqual("");
  });
});

describe("getFinancialYears", () => {
  it("重複", () => {
    const result = getFinancialYears(salaries, 4);
    expect(result).toEqual(["2023", "2024", "2026"]);
  });

  it("空配列", () => {
    const result = getFinancialYears([], 4);
    expect(result).toEqual([]);
  });
});

describe("filterSalaryByFinancialYear", () => {
  it("正常", () => {
    const result = filterSalaryByFinancialYear(salaries, "2024", 4);
    expect(result).toEqual([
      { targetDate: "2024-04-01", overview: { grossIncome: 100000 } },
      { targetDate: "2024-09-01", overview: { grossIncome: 500000 } },
      { targetDate: "2024-08-01", overview: { grossIncome: 600000 } },
      { targetDate: "2024-10-01", overview: { grossIncome: 700000 } },
    ]);
  });

  it("空配列", () => {
    const result = filterSalaryByFinancialYear([], "2024", 4);
    expect(result).toEqual([]);
  });
});

describe("aggregateAnnually", () => {
  it("正常", () => {
    const result = aggregateAnnually(
      salaries,
      (salary) => salary.overview.grossIncome,
      "2024",
      4
    );
    expect(result).toEqual(1900000);
  });

  it("空配列", () => {
    const result = aggregateAnnually(
      [],
      (salary) => salary.overview.grossIncome,
      "2024",
      4
    );
    expect(result).toEqual(0);
  });
});
