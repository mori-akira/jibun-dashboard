import { describe, it, expect } from "vitest";
import type { Salary } from "~/generated/api/client";
import {
  getYearMonthAsNumber,
  getMonthsInFinancialYear,
  getFinancialYear,
  getFinancialYears,
  filterFinancialYears,
  filterSalaryByFinancialYear,
  filterSalaryByFinancialYears,
  filterSalaryByFinancialYearMonth,
  aggregateAnnually,
  aggregateCompareData,
} from "./salary";

const salaries: Salary[] = [
  { targetDate: "2024-04-01", overview: { grossIncome: 100000 } } as Salary,
  { targetDate: "2024-03-01", overview: { grossIncome: 200000 } } as Salary,
  { targetDate: "2026-04-01", overview: { grossIncome: 300000 } } as Salary,
  { targetDate: "2027-03-01", overview: { grossIncome: 400000 } } as Salary,
  { targetDate: "2024-09-01", overview: { grossIncome: 500000 } } as Salary,
  { targetDate: "2024-08-01", overview: { grossIncome: 600000 } } as Salary,
  { targetDate: "2024-10-01", overview: { grossIncome: 700000 } } as Salary,
  { targetDate: "2025-01-01", overview: { grossIncome: 800000 } } as Salary,
];

describe("getYearMonthAsNumber", () => {
  it("年月日", () => {
    const result = getYearMonthAsNumber("2024-04-01");
    expect(result).toEqual([2024, 4]);
  });
  it("年月", () => {
    const result = getYearMonthAsNumber("2024-04");
    expect(result).toEqual([2024, 4]);
  });
  it("年", () => {
    const result = getYearMonthAsNumber("2024");
    expect(result).toEqual([2024, null]);
  });
  it("空文字", () => {
    const result = getYearMonthAsNumber("");
    expect(result).toEqual([null, null]);
  });
});

describe("getMonthsInFinancialYear", () => {
  it("年月", () => {
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

  it("年月日", () => {
    const result = getMonthsInFinancialYear("2024", 4, true);
    expect(result).toEqual([
      "2024-04-01",
      "2024-05-01",
      "2024-06-01",
      "2024-07-01",
      "2024-08-01",
      "2024-09-01",
      "2024-10-01",
      "2024-11-01",
      "2024-12-01",
      "2025-01-01",
      "2025-02-01",
      "2025-03-01",
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

describe("filterFinancialYears", () => {
  it("正常系", () => {
    const input = ["2021", "2022", "2023", "2024"];
    const result = filterFinancialYears(input, "2022");
    expect(result).toEqual(["2021", "2022"]);
  });

  it("対象年未満がない", () => {
    const input = ["2023", "2024"];
    const result = filterFinancialYears(input, "2022");
    expect(result).toEqual([]);
  });

  it("空配列", () => {
    const result = filterFinancialYears([], "2022");
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
      { targetDate: "2025-01-01", overview: { grossIncome: 800000 } },
    ]);
  });

  it("空配列", () => {
    const result = filterSalaryByFinancialYear([], "2024", 4);
    expect(result).toEqual([]);
  });
});

describe("filterSalaryByFinancialYears", () => {
  it("正常", () => {
    const result = filterSalaryByFinancialYears(salaries, ["2023", "2024"], 4);
    expect(result).toEqual([
      { targetDate: "2024-04-01", overview: { grossIncome: 100000 } },
      { targetDate: "2024-03-01", overview: { grossIncome: 200000 } },
      { targetDate: "2024-09-01", overview: { grossIncome: 500000 } },
      { targetDate: "2024-08-01", overview: { grossIncome: 600000 } },
      { targetDate: "2024-10-01", overview: { grossIncome: 700000 } },
      { targetDate: "2025-01-01", overview: { grossIncome: 800000 } },
    ]);
  });

  it("空配列", () => {
    const result = filterSalaryByFinancialYears([], ["2023", "2024"], 4);
    expect(result).toEqual([]);
  });
});

describe("filterSalaryByFinancialYearMonth", () => {
  it("ヒットあり(年月)", () => {
    const result = filterSalaryByFinancialYearMonth(salaries, "2026-04");
    expect(result).toEqual({
      targetDate: "2026-04-01",
      overview: { grossIncome: 300000 },
    });
  });

  it("ヒットあり(年月日)", () => {
    const result = filterSalaryByFinancialYearMonth(salaries, "2026-04-01");
    expect(result).toEqual({
      targetDate: "2026-04-01",
      overview: { grossIncome: 300000 },
    });
  });

  it("ヒットなし(年月日)", () => {
    const result = filterSalaryByFinancialYearMonth(salaries, "2026-04-02");
    expect(result).toEqual(undefined);
  });

  it("空配列", () => {
    const result = filterSalaryByFinancialYearMonth([], "2026-04");
    expect(result).toEqual(undefined);
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
    expect(result).toEqual(2700000);
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

describe("aggregateCompareData", () => {
  const salaries: Salary[] = [
    {
      targetDate: "2024-04-01",
      overview: {
        grossIncome: 100000,
        netIncome: 80000,
        operatingTime: 160,
        overtime: 20,
        bonus: 0,
        bonusTakeHome: 0,
      },
    } as Salary,
    {
      targetDate: "2024-05-01",
      overview: {
        grossIncome: 110000,
        netIncome: 85000,
        operatingTime: 160,
        overtime: 25,
        bonus: 0,
        bonusTakeHome: 0,
      },
    } as Salary,
  ];

  it("複数年分のデータを正しく集計できる", () => {
    const result = aggregateCompareData(
      salaries,
      ["2024"],
      "grossIncome",
      ["#ff0000"],
      4
    );
    expect(result).toHaveLength(1);
    expect(result[0]!.label).toBe("FY2024");
    expect(result[0]!.backgroundColor).toBe("#ff0000");
    expect(result[0]!.data.length).toBe(12);
    expect(result[0]!.data[0]).toBe(100000);
    expect(result[0]!.data[1]).toBe(110000);
  });

  it("対象データが存在しない場合は0で埋まる", () => {
    const result = aggregateCompareData(
      [], // 空データ
      ["2024"],
      "grossIncome",
      ["#00ff00"],
      4
    );
    expect(result[0]!.data.every((val: number) => val === 0)).toBe(true);
  });
});
