import type { Salary } from "~/api/client";

function getYearMonthAsNumber(date: string): (number | null)[] {
  const [year, month] = date.split("-");
  return [year ? parseInt(year) : null, month ? parseInt(month) : null];
}

export function getMonthsInFinancialYear(
  financialYear: string,
  financialYearStartMonth: number,
  day?: number
): string[] {
  return Array.from({ length: 12 }, (_, i) => {
    const month = ((i + financialYearStartMonth - 1) % 12) + 1;
    const year =
      month < financialYearStartMonth
        ? parseInt(financialYear) + 1
        : parseInt(financialYear);
    const monthStr = String(month).padStart(2, "0");
    const dayStr = day !== undefined ? `-${String(day).padStart(2, "0")}` : "";
    return day ? `${year}-${monthStr}-${dayStr}` : `${year}-${monthStr}`;
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

export function filterSalaryByFinancialYear(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number
): Salary[] {
  return salaries.filter(
    (e) => getFinancialYear(e, financialYearStartMonth) === targetYear
  );
}

export function getTotalAnnualIncome(
  salaries: Salary[],
  targetYear: string,
  financialYearStartMonth: number
): number {
  return filterSalaryByFinancialYear(
    salaries,
    targetYear,
    financialYearStartMonth
  ).reduce((total: number, cv: Salary) => total + cv.overview.grossIncome, 0);
}
