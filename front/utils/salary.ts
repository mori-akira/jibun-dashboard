import type { Overview, Salary, Structure } from "~/generated/api/client";
import type { DataSet } from "~/components/common/graph/types";
import { getShortMonthLabel } from "./date";

/**
 * 給与構成の表示定義。
 * 色は積み上げ順に固定で割り当てる(色相を使い回さない)。
 */
export const structureDef: {
  key: keyof Structure;
  label: string;
  color: string;
}[] = [
  { key: "basicSalary", label: "Basic Salary", color: "#2a78d6" },
  { key: "overtimePay", label: "Overtime Pay", color: "#eb6834" },
  { key: "housingAllowance", label: "Housing Allowance", color: "#1baf7a" },
  { key: "positionAllowance", label: "Position Allowance", color: "#eda100" },
  { key: "other", label: "Other", color: "#e87ba4" },
];

export type StructureSummary = {
  key: keyof Structure;
  label: string;
  color: string;
  total: number;
  share: number;
};

export function getYearMonthAsNumber(date: string): (number | null)[] {
  const [year, month] = date.split("-");
  return [year ? parseInt(year) : null, month ? parseInt(month) : null];
}

export function getMonthsInFinancialYear(
  financialYear: string,
  financialYearStartMonth: number,
  withFirstDay?: boolean,
): string[] {
  return Array.from({ length: 12 }, (_, i) => {
    const month = ((i + financialYearStartMonth - 1) % 12) + 1;
    const year =
      month < financialYearStartMonth
        ? parseInt(financialYear) + 1
        : parseInt(financialYear);
    const monthStr = String(month).padStart(2, "0");
    return withFirstDay ? `${year}-${monthStr}-01` : `${year}-${monthStr}`;
  });
}

export function getFinancialYear(
  salary: Salary,
  financialYearStartMonth: number,
): string {
  const [year, month] = getYearMonthAsNumber(salary.targetDate);
  if (year == null || month == null) {
    return "";
  }
  if (month < financialYearStartMonth) {
    return `${year - 1}`;
  }
  return `${year}`;
}

export function getFinancialYears(
  salaries: Salary[],
  financialYearStartMonth: number,
): string[] {
  const years = salaries.map((e) =>
    getFinancialYear(e, financialYearStartMonth),
  );
  const uniqueYears = Array.from(new Set(years));
  return uniqueYears.sort();
}

export function filterFinancialYears(
  financialYears: string[],
  untilYear: string,
): string[] {
  return financialYears.filter((year) => year <= untilYear);
}

export function filterSalaryByFinancialYear(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number,
): Salary[] {
  return filterSalaryByFinancialYears(
    salaries,
    [targetYear],
    financialYearStartMonth,
  );
}

export function filterSalaryByFinancialYears(
  salaries: Salary[],
  targetYear: string[],
  financialYearStartMonth: number,
): Salary[] {
  return salaries.filter((e) =>
    targetYear.includes(getFinancialYear(e, financialYearStartMonth)),
  );
}

export function filterSalaryByFinancialYearMonth(
  salaries: Salary[],
  targetYearMonth: string,
): Salary | undefined {
  const targetDate = targetYearMonth.match(/^\d{4}-\d{2}$/)
    ? `${targetYearMonth}-01`
    : targetYearMonth;
  const filtered = salaries.filter((e) => e.targetDate === targetDate);
  return filtered.length > 0 ? filtered[0] : undefined;
}

export function aggregateAnnually(
  salaries: Salary[],
  selector: (salary: Salary) => number,
  targetYear: string,
  financialYearStartMonth: number,
): number {
  return filterSalaryByFinancialYear(
    salaries,
    targetYear,
    financialYearStartMonth,
  ).reduce((total: number, cv: Salary) => total + selector(cv), 0);
}

export function aggregateCompareData(
  salaries: Salary[],
  targetYears: string[],
  label: keyof Overview,
  backgroundColors: string[],
  financialYearStartMonth: number,
): DataSet[] {
  return targetYears.map((year, index) => {
    return {
      label: `FY${year}`,
      backgroundColor: backgroundColors[index] ?? "#ddd",
      data: getMonthsInFinancialYear(year, financialYearStartMonth, true).map(
        (date) => {
          const salary = filterSalaryByFinancialYearMonth(salaries ?? [], date);
          return salary ? salary?.overview?.[label] : 0;
        },
      ),
    };
  });
}

/**
 * 月ごとのバー色を組み立てる。選択中の月以外は減光する。
 *
 * 未選択でも単色文字列ではなく必ず配列を返すこと。
 * Chart.js は backgroundColor が文字列だと解決済みオプションを
 * DatasetController._sharedOptions にキャッシュし、これは configure() でも破棄されない。
 * 配列(要素ごとの色)と文字列を行き来させると、配列から文字列に戻したときに
 * 古い共有オプションが再利用されて減光状態が残る。
 */
export function buildMonthlyColors(
  color: string,
  monthCount: number,
  selectedMonthIndex?: number | null,
): string[] {
  return Array.from({ length: monthCount }, (_, index) =>
    selectedMonthIndex == null || index === selectedMonthIndex
      ? color
      : `${color}33`,
  );
}

export function getMonthLabelsInFinancialYear(
  financialYearStartMonth: number,
): string[] {
  return Array.from({ length: 12 }, (_, i) =>
    getShortMonthLabel(((i + financialYearStartMonth - 1) % 12) + 1),
  );
}

/**
 * 年度内の月ごとに給与構成を集計する。
 * asRatio 指定時は月内合計を 100 とした構成比(%)を返す。
 */
export function aggregateStructureData(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number,
  options?: { asRatio?: boolean },
): DataSet[] {
  const monthlySalaries = getMonthsInFinancialYear(
    targetYear,
    financialYearStartMonth,
    true,
  ).map((date) => filterSalaryByFinancialYearMonth(salaries, date));
  const monthlyTotals = monthlySalaries.map((salary) =>
    structureDef.reduce(
      (total, def) => total + (salary?.structure?.[def.key] ?? 0),
      0,
    ),
  );
  return structureDef.map((def) => ({
    label: def.label,
    backgroundColor: def.color,
    data: monthlySalaries.map((salary, index) => {
      const value = salary?.structure?.[def.key] ?? 0;
      if (!options?.asRatio) {
        return value;
      }
      const total = monthlyTotals[index] ?? 0;
      return total === 0 ? 0 : (value / total) * 100;
    }),
  }));
}

function toStructureSummary(salaries: Salary[]): StructureSummary[] {
  const totals = structureDef.map((def) =>
    salaries.reduce(
      (total, salary) => total + (salary?.structure?.[def.key] ?? 0),
      0,
    ),
  );
  const grandTotal = totals.reduce((total, value) => total + value, 0);
  return structureDef.map((def, index) => {
    const total = totals[index] ?? 0;
    return {
      ...def,
      total,
      share: grandTotal === 0 ? 0 : (total / grandTotal) * 100,
    };
  });
}

/**
 * 年度内の給与構成を項目ごとに合計し、構成比を添えて返す。
 */
export function aggregateStructureSummary(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number,
): StructureSummary[] {
  return toStructureSummary(
    filterSalaryByFinancialYear(salaries, targetYear, financialYearStartMonth),
  );
}

/**
 * 年度内の指定月(年度開始月からの 0 始まりの位置)の給与構成を集計する。
 * 該当月のデータが無い場合は合計・構成比とも 0 を返す。
 */
export function aggregateStructureSummaryOfMonth(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number,
  monthIndex: number,
): StructureSummary[] {
  const targetMonth = getMonthsInFinancialYear(
    targetYear,
    financialYearStartMonth,
    true,
  )[monthIndex];
  const targetSalary = targetMonth
    ? filterSalaryByFinancialYearMonth(salaries, targetMonth)
    : undefined;
  return toStructureSummary(targetSalary ? [targetSalary] : []);
}
