import type { Overview, Salary } from "~/api/client";
import type { DataSet as CompareBarGraphDataSet } from "~/components/common/graph/CompareBar.vue";

export function getYearMonthAsNumber(date: string): (number | null)[] {
  const [year, month] = date.split("-");
  return [year ? parseInt(year) : null, month ? parseInt(month) : null];
}

export function getMonthsInFinancialYear(
  financialYear: string,
  financialYearStartMonth: number,
  withFirstDay?: boolean
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
  financialYearStartMonth: number
): string {
  const [year, month] = getYearMonthAsNumber(salary.targetDate);
  if (year === null || month === null) {
    return "";
  }
  if (month < financialYearStartMonth) {
    return `${year - 1}`;
  }
  return `${year}`;
}

export function getFinancialYears(
  salaries: Salary[],
  financialYearStartMonth: number
): string[] {
  const years = salaries.map((e) =>
    getFinancialYear(e, financialYearStartMonth)
  );
  const uniqueYears = Array.from(new Set(years));
  return uniqueYears.sort();
}

export function filterFinancialYears(
  financialYears: string[],
  untilYear: string
): string[] {
  return financialYears.filter((year) => year <= untilYear);
}

export function filterSalaryByFinancialYear(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number
): Salary[] {
  return filterSalaryByFinancialYears(
    salaries,
    [targetYear],
    financialYearStartMonth
  );
}

export function filterSalaryByFinancialYears(
  salaries: Salary[],
  targetYear: string[],
  financialYearStartMonth: number
): Salary[] {
  return salaries.filter((e) =>
    targetYear.includes(getFinancialYear(e, financialYearStartMonth))
  );
}

export function filterSalaryByFinancialYearMonth(
  salaries: Salary[],
  targetYearMonth: string
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
  financialYearStartMonth: number
): number {
  return filterSalaryByFinancialYear(
    salaries,
    targetYear,
    financialYearStartMonth
  ).reduce((total: number, cv: Salary) => total + selector(cv), 0);
}

export function aggregateCompareData(
  salaries: Salary[],
  targetYears: string[],
  label: keyof Overview,
  backgroundColors: string[],
  financialYearStartMonth: number
): CompareBarGraphDataSet[] {
  return targetYears.map((year, index) => {
    return {
      label: `FY${year}`,
      backgroundColor: backgroundColors[index],
      data: getMonthsInFinancialYear(year, financialYearStartMonth, true).map(
        (date) => {
          const salary = filterSalaryByFinancialYearMonth(salaries ?? [], date);
          return salary ? salary?.overview?.[label] : 0;
        }
      ),
    };
  });
}
