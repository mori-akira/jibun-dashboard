import type { Salary } from "~/api/client";

export function getYears(salaries: Salary[]): string[] {
  const years = salaries.map((e) => e.targetDate.substring(0, 4));
  const uniqueYears = Array.from(new Set(years));
  return uniqueYears.sort();
}

export function getLatestYear(salaries: Salary[]): string | undefined {
  const years = getYears(salaries);
  return years.length > 0 ? years[years.length - 1] : undefined;
}

export function filterSalaryByYear(
  salaries: Salary[],
  targetYear: string
): Salary[] {
  return salaries.filter((e) => e.targetDate.startsWith(targetYear));
}

export function getTotalYearIncome(
  salaries: Salary[],
  targetYear: string
): number {
  return filterSalaryByYear(salaries, targetYear).reduce(
    (total, cv) => total + cv.overview.grossIncome,
    0
  );
}
