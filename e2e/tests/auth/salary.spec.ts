import { test, expect } from "@playwright/test";
import testData from "./salary.spec.data.json";

const currentDate = new Date();
const currentYear = currentDate.getFullYear();
const currentMonth = currentDate.getMonth() + 1;

test("test salary function", async ({ page }) => {
  // access to salary
  await page.goto("/salary", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // access to edit
  await Promise.all([
    page.waitForURL("**/salary/edit"),
    page.getByRole("button", { name: "Edit" }).click(),
  ]);
  await expect(page.getByText("Edit", { exact: true })).toBeVisible();

  // check target date
  await expect(page.locator("[data-test-id='dp-input']")).toHaveValue(
    `${currentYear}-${String(currentMonth).padStart(2, "0")}-01`
  );

  // select April
  await page.locator('[data-test-id="dp-input"]').click();
  await page.getByText("Apr").click();
  await page.locator('[data-test-id="select-button"]').click();

  // check target date
  await expect(page.locator("[data-test-id='dp-input']")).toHaveValue(
    `${currentYear}-04-01`
  );

  // input overview
  await page
    .locator("div")
    .filter({ hasText: /^Overview$/ })
    .click();
  await fillOverview(page, testData[0].overview);

  // input structure
  await page
    .locator("div")
    .filter({ hasText: /^Structure$/ })
    .click();
  await fillStructure(page, testData[0].structure);

  // input payslip data
  await addPayslipData(page, testData[0].payslipData);

  // input dummy payslip data
  await addPayslipData(page, [
    {
      key: "dummy",
      data: [
        { key: "dummy1", data: 1111 },
        { key: "dummy2", data: 2222 },
      ],
    },
  ]);

  // delete payslip: dummy2
  await page
    .locator(
      "div:nth-child(6) > .body-wrapper > .body > div:nth-child(2) > .w-10 > .iconify"
    )
    .click();
  await expect(page.getByText("dummy1", { exact: true })).toBeVisible();
  await expect(page.getByText("dummy2", { exact: true })).not.toBeVisible();

  // delete category: dummy
  await page
    .getByRole("button", { name: "Delete Category", exact: true })
    .last()
    .click();
  await page.getByRole("button", { name: "Yes", exact: true }).click();
  await expect(
    page.locator("div").filter({ hasText: /^dummy$/ })
  ).not.toBeVisible();

  // execute
  await page.getByRole("button", { name: "Execute", exact: true }).click();
  await page.getByRole("button", { name: "OK" }).click();

  // add salary simply: May
  await addSalarySimply(
    page,
    "May",
    `${currentYear}-05-01`,
    false,
    testData[1]
  );

  // add salary simply: January
  await addSalarySimply(
    page,
    "Jan",
    `${currentYear}-01-01`,
    false,
    testData[2]
  );

  // add salary simply: February
  await addSalarySimply(
    page,
    "Feb",
    `${currentYear}-02-01`,
    false,
    testData[3]
  );

  // add salary simply: last year's January
  await addSalarySimply(
    page,
    "Jan",
    `${currentYear - 1}-01-01`,
    true,
    testData[4]
  );

  // add salary simply: February
  await addSalarySimply(
    page,
    "Feb",
    `${currentYear - 1}-02-01`,
    false,
    testData[5]
  );

  // === salary dashboard ===
  await page.getByRole("main").getByRole("link", { name: "Salary" }).click();

  // check year summary
  await expect(
    page.locator('span:has-text("This Year") + span').nth(0)
  ).toHaveText("￥614,000");
  await expect(
    page.locator('span:has-text("Last Year") + span').nth(0)
  ).toHaveText("￥736,000");
  await expect(
    page.locator('span:has-text("This Year") + span').nth(1)
  ).toHaveText("72 H");
  await expect(
    page.locator('span:has-text("Last Year") + span').nth(1)
  ).toHaveText("133 H");

  // check fiscal year summary
  await expect(
    page.locator(`span:has-text("FY${currentYear}") + span`)
  ).toHaveText("614,000");
  await expect(
    page.locator(`span:has-text("FY${currentYear - 1}") + span`)
  ).toHaveText("736,000");
  await expect(
    page.locator(`span:has-text("FY${currentYear - 2}") + span`)
  ).toHaveText("546,000");

  // check payslip
  // May of this year
  await expect(page.locator("h4").nth(0)).toHaveText("May");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(0)
  ).toHaveText("235,000");
  await expect(
    page.locator(`div.label:has-text("Net Income") + div`).nth(0)
  ).toHaveText("166,850");
  await expect(
    page.locator(`div.label:has-text("Operating Time") + div`).nth(0)
  ).toHaveText("144");
  await expect(
    page.locator(`div.label:has-text("Basic Salary") + div`).nth(0)
  ).toHaveText("200,000");
  await expect(
    page.locator(`div.label:has-text("Housing Allowance") + div`).nth(0)
  ).toHaveText("30,000");
  await expect(
    page.locator(`div.label:has-text("Other") + div`).nth(0)
  ).toHaveText("5,000");
  await expect(
    page.locator(`div.label:has-text("稼働時間") + div`).nth(0)
  ).toHaveText("144");
  await expect(
    page.locator(`div.label:has-text("残業時間") + div`).nth(0)
  ).toHaveText("0");
  await expect(
    page.locator(`div.label:has-text("有給取得") + div`).nth(0)
  ).toHaveText("3");
  await expect(
    page.locator(`div.label:has-text("基本給") + div`).nth(0)
  ).toHaveText("200,000");
  await expect(
    page.locator(`div.label:has-text("残業代") + div`).nth(0)
  ).toHaveText("0");
  await expect(
    page.locator(`div.label:has-text("住宅手当") + div`).nth(0)
  ).toHaveText("30,000");
  await expect(
    page.locator(`div.label:has-text("その他") + div`).nth(0)
  ).toHaveText("5,000");
  await expect(
    page.locator(`div.label:has-text("ボーナス") + div`).nth(0)
  ).toHaveText("0");
  await expect(
    page.locator(`div.label:has-text("社会保険料") + div`).nth(0)
  ).toHaveText("35,250");
  await expect(
    page.locator(`div.label:has-text("健康保険料") + div`).nth(0)
  ).toHaveText("11,750");
  await expect(
    page.locator(`div.label:has-text("所得税") + div`).nth(0)
  ).toHaveText("14,100");
  await expect(
    page.locator(`div.label:has-text("住民税") + div`).nth(0)
  ).toHaveText("7,050");

  // April of this year
  await expect(page.locator("h4").nth(1)).toHaveText("Apr");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(1)
  ).toHaveText("379,000");
  await expect(
    page.locator(`div.label:has-text("Net Income") + div`).nth(1)
  ).toHaveText("269,091");
  await expect(
    page.locator(`div.label:has-text("Operating Time") + div`).nth(1)
  ).toHaveText("216");
  await expect(
    page
      .locator("div.label")
      .filter({ hasText: /^Overtime$/ })
      .locator("xpath=following-sibling::div")
      .nth(0)
  ).toHaveText("72");
  await expect(
    page.locator(`div.label:has-text("Basic Salary") + div`).nth(1)
  ).toHaveText("200,000");
  await expect(
    page.locator(`div.label:has-text("Overtime Pay") + div`).nth(0)
  ).toHaveText("144,000");
  await expect(
    page.locator(`div.label:has-text("Housing Allowance") + div`).nth(1)
  ).toHaveText("30,000");
  await expect(
    page.locator(`div.label:has-text("Other") + div`).nth(1)
  ).toHaveText("5,000");
  await expect(
    page.locator(`div.label:has-text("稼働時間") + div`).nth(1)
  ).toHaveText("144");
  await expect(
    page.locator(`div.label:has-text("残業時間") + div`).nth(1)
  ).toHaveText("72");
  await expect(
    page.locator(`div.label:has-text("有給取得") + div`).nth(1)
  ).toHaveText("0");
  await expect(
    page.locator(`div.label:has-text("基本給") + div`).nth(1)
  ).toHaveText("200,000");
  await expect(
    page.locator(`div.label:has-text("残業代") + div`).nth(1)
  ).toHaveText("144,000");
  await expect(
    page.locator(`div.label:has-text("住宅手当") + div`).nth(1)
  ).toHaveText("30,000");
  await expect(
    page.locator(`div.label:has-text("その他") + div`).nth(1)
  ).toHaveText("5,000");
  await expect(
    page.locator(`div.label:has-text("ボーナス") + div`).nth(1)
  ).toHaveText("0");
  await expect(
    page.locator(`div.label:has-text("社会保険料") + div`).nth(1)
  ).toHaveText("56,850");
  await expect(
    page.locator(`div.label:has-text("健康保険料") + div`).nth(1)
  ).toHaveText("18,950");
  await expect(
    page.locator(`div.label:has-text("所得税") + div`).nth(1)
  ).toHaveText("22,740");
  await expect(
    page.locator(`div.label:has-text("住民税") + div`).nth(1)
  ).toHaveText("11,370");

  // February of last year
  await expect(page.locator("h4").nth(2)).toHaveText("Feb");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(2)
  ).toHaveText("375,000");
  await expect(
    page.locator(`div.label:has-text("Net Income") + div`).nth(2)
  ).toHaveText("266,251");
  await expect(
    page.locator(`div.label:has-text("Operating Time") + div`).nth(2)
  ).toHaveText("230");
  await expect(
    page
      .locator("div.label")
      .filter({ hasText: /^Overtime$/ })
      .locator("xpath=following-sibling::div")
      .nth(1)
  ).toHaveText("70");
  await expect(
    page.locator(`div.label:has-text("Basic Salary") + div`).nth(2)
  ).toHaveText("200,000");
  await expect(
    page.locator(`div.label:has-text("Overtime Pay") + div`).nth(1)
  ).toHaveText("140,000");
  await expect(
    page.locator(`div.label:has-text("Housing Allowance") + div`).nth(2)
  ).toHaveText("30,000");
  await expect(
    page.locator(`div.label:has-text("Other") + div`).nth(2)
  ).toHaveText("5,000");
  await expect(
    page.locator(`div.label:has-text("稼働時間") + div`).nth(2)
  ).toHaveText("160");
  await expect(
    page.locator(`div.label:has-text("残業時間") + div`).nth(2)
  ).toHaveText("70");
  await expect(
    page.locator(`div.label:has-text("有給取得") + div`).nth(2)
  ).toHaveText("3");
  await expect(
    page.locator(`div.label:has-text("基本給") + div`).nth(2)
  ).toHaveText("200,000");
  await expect(
    page.locator(`div.label:has-text("残業代") + div`).nth(2)
  ).toHaveText("140,000");
  await expect(
    page.locator(`div.label:has-text("住宅手当") + div`).nth(2)
  ).toHaveText("30,000");
  await expect(
    page.locator(`div.label:has-text("その他") + div`).nth(2)
  ).toHaveText("5,000");
  await expect(
    page.locator(`div.label:has-text("ボーナス") + div`).nth(2)
  ).toHaveText("0");
  await expect(
    page.locator(`div.label:has-text("社会保険料") + div`).nth(2)
  ).toHaveText("56,250");
  await expect(
    page.locator(`div.label:has-text("健康保険料") + div`).nth(2)
  ).toHaveText("18,750");
  await expect(
    page.locator(`div.label:has-text("所得税") + div`).nth(2)
  ).toHaveText("22,500");
  await expect(
    page.locator(`div.label:has-text("住民税") + div`).nth(2)
  ).toHaveText("11,250");

  // === salary payslip ===
  await page.getByRole("button", { name: "Payslip" }).click();

  // May of this year
  await expect(page.locator("h4").nth(0)).toHaveText("May");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(0)
  ).toHaveText("235,000");

  // Apr of this year
  await expect(page.locator("h4").nth(1)).toHaveText("Apr");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(1)
  ).toHaveText("379,000");

  // Feb of last year
  await expect(page.locator("h4").nth(2)).toHaveText("Feb");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(2)
  ).toHaveText("375,000");

  // Jan of last year
  await expect(page.locator("h4").nth(3)).toHaveText("Jan");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(3)
  ).toHaveText("361,000");

  // Feb of year before last
  await expect(page.locator("h4").nth(4)).toHaveText("Feb");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(4)
  ).toHaveText("235,000");

  // Jan of year before last
  await expect(page.locator("h4").nth(5)).toHaveText("Jan");
  await expect(
    page.locator(`div.label:has-text("Gross Income") + div`).nth(5)
  ).toHaveText("311,000");

  // delete salary data
  await page.goto("/salary/edit", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
  await deleteSalary(page, "Jan");
  await deleteSalary(page, "Feb");
  await deleteSalary(page, "Apr");
  await deleteSalary(page, "May");
  await deleteSalary(page, "Jan", true);
  await deleteSalary(page, "Feb");
});

type Overview = {
  grossIncome: number;
  netIncome: number;
  operatingTime: number;
  overtime: number;
  bonus: number;
  bonusTakeHome: number;
};

type Structure = {
  basicSalary: number;
  overtimePay: number;
  housingAllowance: number;
  positionAllowance: number;
  other: number;
};

type Payslip = {
  key: string;
  data: number;
};

type PayslipData = {
  key: string;
  data: Payslip[];
};

const fillOverview = async (page: any, overview: Overview) => {
  await page
    .getByRole("spinbutton", { name: "Gross Income", exact: true })
    .fill(overview.grossIncome.toString());
  await page
    .getByRole("spinbutton", { name: "Net Income", exact: true })
    .fill(overview.netIncome.toString());
  await page
    .getByRole("spinbutton", { name: "Operating Time", exact: true })
    .fill(overview.operatingTime.toString());
  await page
    .getByRole("spinbutton", { name: "Overtime", exact: true })
    .fill(overview.overtime.toString());
  await page
    .getByRole("spinbutton", { name: "Bonus", exact: true })
    .fill(overview.bonus.toString());
  await page
    .getByRole("spinbutton", { name: "Bonus Take Home", exact: true })
    .fill(overview.bonusTakeHome.toString());
};

const fillStructure = async (page: any, structure: Structure) => {
  await page
    .getByRole("spinbutton", { name: "Basic Salary", exact: true })
    .fill(structure.basicSalary.toString());
  await page
    .getByRole("spinbutton", { name: "Overtime Pay", exact: true })
    .fill(structure.overtimePay.toString());
  await page
    .getByRole("spinbutton", { name: "Housing Allowance", exact: true })
    .fill(structure.housingAllowance.toString());
  await page
    .getByRole("spinbutton", { name: "Positional Allowance", exact: true })
    .fill(structure.positionAllowance.toString());
  await page
    .getByRole("spinbutton", { name: "Other", exact: true })
    .fill(structure.other.toString());
};

const addPayslipData = async (page: any, payslipData: PayslipData[]) => {
  for (const category of payslipData) {
    await page.getByRole("button", { name: "Add Category" }).click();
    await page.getByRole("textbox", { name: "*" }).fill(category.key);
    await page.getByRole("button", { name: "OK" }).click();
    await page
      .locator("div")
      .filter({ hasText: new RegExp(`^${category.key}$`) })
      .first()
      .click();
    for (const payslip of category.data) {
      await page.getByRole("button", { name: "Add Payslip" }).last().click();
      await page.getByRole("textbox", { name: "*" }).fill(payslip.key);
      await page.getByRole("button", { name: "OK" }).click();
      await page
        .getByRole("spinbutton", { name: payslip.key })
        .fill(payslip.data.toString());
    }
  }
};

const addSalarySimply = async (
  page: any,
  month: string,
  expectedDate: string,
  previousYear: boolean = false,
  salaryData: {
    overview: Overview;
    structure: Structure;
    payslipData: PayslipData[];
  }
) => {
  // select month
  await page.locator('[data-test-id="dp-input"]').click();
  if (previousYear) {
    await page.getByRole("button", { name: "Previous year" }).click();
  }
  await page.getByText(month).click();
  await page.locator('[data-test-id="select-button"]').click();

  // check target date
  await expect(page.locator("[data-test-id='dp-input']")).toHaveValue(
    expectedDate
  );

  // wait for few seconds
  await page.waitForTimeout(500);

  // input
  await fillOverview(page, salaryData.overview);
  await fillStructure(page, salaryData.structure);
  await addPayslipData(page, salaryData.payslipData);

  // execute
  await page.getByRole("button", { name: "Execute", exact: true }).click();
  await page.getByRole("button", { name: "OK" }).click();
};

const deleteSalary = async (
  page: any,
  month: string,
  previousYear: boolean = false
) => {
  await page.locator('[data-test-id="dp-input"]').click();
  if (previousYear) {
    await page.getByRole("button", { name: "Previous year" }).click();
  }
  await page.getByText(month).click();
  await page.locator('[data-test-id="select-button"]').click();
  await page.getByRole("button", { name: "Delete", exact: true }).click();
  await page.getByRole("button", { name: "Yes", exact: true }).click();
  await page.getByRole("button", { name: "OK", exact: true }).click();
};
