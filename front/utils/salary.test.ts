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
  getMonthLabelsInFinancialYear,
  aggregateStructureData,
  aggregateStructureSummary,
  aggregateStructureSummaryOfMonth,
  buildMonthlyColors,
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
      4,
    );
    expect(result).toEqual(2700000);
  });

  it("空配列", () => {
    const result = aggregateAnnually(
      [],
      (salary) => salary.overview.grossIncome,
      "2024",
      4,
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
      4,
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
      4,
    );
    expect(result[0]!.data.every((val: number) => val === 0)).toBe(true);
  });
});

describe("getMonthLabelsInFinancialYear", () => {
  it("年度開始月から12ヶ月分のラベルを返す", () => {
    expect(getMonthLabelsInFinancialYear(4)).toEqual([
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec",
      "Jan",
      "Feb",
      "Mar",
    ]);
  });

  it("1月開始の場合は暦年と一致する", () => {
    expect(getMonthLabelsInFinancialYear(1)[0]).toBe("Jan");
    expect(getMonthLabelsInFinancialYear(1)[11]).toBe("Dec");
  });
});

describe("aggregateStructureData / aggregateStructureSummary", () => {
  const structureSalaries: Salary[] = [
    {
      targetDate: "2024-04-01",
      structure: {
        basicSalary: 300000,
        overtimePay: 100000,
        housingAllowance: 50000,
        positionAllowance: 30000,
        other: 20000,
      },
    } as Salary,
    {
      targetDate: "2024-05-01",
      structure: {
        basicSalary: 300000,
        overtimePay: 50000,
        housingAllowance: 50000,
        positionAllowance: 0,
        other: 0,
      },
    } as Salary,
  ];

  it("構成項目ごとに12ヶ月分のデータセットを返す", () => {
    const result = aggregateStructureData(structureSalaries, "2024", 4);
    expect(result).toHaveLength(5);
    expect(result[0]!.label).toBe("Basic Salary");
    expect(result[0]!.data).toHaveLength(12);
    expect(result[0]!.data[0]).toBe(300000);
    expect(result[1]!.data[0]).toBe(100000);
    expect(result[3]!.data[1]).toBe(0);
  });

  it("データのない月は0で埋まる", () => {
    const result = aggregateStructureData(structureSalaries, "2024", 4);
    expect(result.every((dataset) => dataset.data[2] === 0)).toBe(true);
  });

  it("asRatio 指定時は月内合計を100とした構成比を返す", () => {
    const result = aggregateStructureData(structureSalaries, "2024", 4, {
      asRatio: true,
    });
    expect(result[0]!.data[0]).toBeCloseTo(60);
    expect(result[1]!.data[0]).toBeCloseTo(20);
    const total = result.reduce(
      (sum, dataset) => sum + (dataset.data[0] ?? 0),
      0,
    );
    expect(total).toBeCloseTo(100);
  });

  it("合計0の月は0除算せず0を返す", () => {
    const result = aggregateStructureData(structureSalaries, "2024", 4, {
      asRatio: true,
    });
    expect(result.every((dataset) => dataset.data[2] === 0)).toBe(true);
  });

  it("年度合計と構成比を集計する", () => {
    const result = aggregateStructureSummary(structureSalaries, "2024", 4);
    expect(result).toHaveLength(5);
    expect(result[0]!.total).toBe(600000);
    expect(result[1]!.total).toBe(150000);
    const shareTotal = result.reduce((sum, item) => sum + item.share, 0);
    expect(shareTotal).toBeCloseTo(100);
  });

  it("対象データが無い場合は合計も構成比も0", () => {
    const result = aggregateStructureSummary([], "2024", 4);
    expect(result.every((item) => item.total === 0 && item.share === 0)).toBe(
      true,
    );
  });
});

describe("aggregateStructureSummaryOfMonth", () => {
  const monthlySalaries: Salary[] = [
    {
      targetDate: "2024-04-01",
      structure: {
        basicSalary: 300000,
        overtimePay: 100000,
        housingAllowance: 0,
        positionAllowance: 0,
        other: 0,
      },
    } as Salary,
    {
      targetDate: "2024-05-01",
      structure: {
        basicSalary: 200000,
        overtimePay: 0,
        housingAllowance: 0,
        positionAllowance: 0,
        other: 0,
      },
    } as Salary,
  ];

  it("年度開始月からの位置で対象月を特定する", () => {
    const result = aggregateStructureSummaryOfMonth(
      monthlySalaries,
      "2024",
      4,
      0,
    );
    expect(result[0]!.total).toBe(300000);
    expect(result[0]!.share).toBeCloseTo(75);
    expect(result[1]!.total).toBe(100000);
    expect(result[1]!.share).toBeCloseTo(25);
  });

  it("2番目の月は翌月のデータを返す", () => {
    const result = aggregateStructureSummaryOfMonth(
      monthlySalaries,
      "2024",
      4,
      1,
    );
    expect(result[0]!.total).toBe(200000);
    expect(result[0]!.share).toBeCloseTo(100);
  });

  it("年をまたぐ月も年度内として扱う", () => {
    const januarySalary = [
      {
        targetDate: "2025-01-01",
        structure: {
          basicSalary: 111000,
          overtimePay: 0,
          housingAllowance: 0,
          positionAllowance: 0,
          other: 0,
        },
      } as Salary,
    ];
    const result = aggregateStructureSummaryOfMonth(januarySalary, "2024", 4, 9);
    expect(result[0]!.total).toBe(111000);
  });

  it("データの無い月は合計も構成比も0", () => {
    const result = aggregateStructureSummaryOfMonth(
      monthlySalaries,
      "2024",
      4,
      5,
    );
    expect(result.every((item) => item.total === 0 && item.share === 0)).toBe(
      true,
    );
  });

  it("範囲外の位置を指定しても落ちない", () => {
    const result = aggregateStructureSummaryOfMonth(
      monthlySalaries,
      "2024",
      4,
      12,
    );
    expect(result.every((item) => item.total === 0)).toBe(true);
  });
});

describe("buildMonthlyColors", () => {
  it("未選択時は全月が原色", () => {
    const result = buildMonthlyColors("#2a78d6", 12, null);
    expect(result).toHaveLength(12);
    expect(result.every((color) => color === "#2a78d6")).toBe(true);
  });

  it("選択時は対象月のみ原色、他は減光", () => {
    const result = buildMonthlyColors("#2a78d6", 12, 2);
    expect(result[2]).toBe("#2a78d6");
    expect(result[0]).toBe("#2a78d633");
    expect(result.filter((color) => color === "#2a78d6")).toHaveLength(1);
  });

  it("選択状態によらず常に配列を返す(Chart.js の共有オプション対策)", () => {
    // 文字列と配列を行き来させると選択解除時に減光が残るため、型を揺らさないこと
    expect(Array.isArray(buildMonthlyColors("#2a78d6", 12, null))).toBe(true);
    expect(Array.isArray(buildMonthlyColors("#2a78d6", 12, 0))).toBe(true);
  });

  it("undefined も未選択として扱う", () => {
    const result = buildMonthlyColors("#2a78d6", 3, undefined);
    expect(result).toEqual(["#2a78d6", "#2a78d6", "#2a78d6"]);
  });
});
