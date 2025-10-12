import { test, expect } from "@playwright/test";
import testData from "./salary.spec.data.json";

const currentDate = new Date();
const currentYear = currentDate.getFullYear();
const currentMonth = currentDate.getMonth() + 1;

test("check salary function", async ({ page }) => {
  const username = process.env.E2E_USERNAME;
  if (!username) {
    throw new Error("E2E_USERNAME is not set");
  }

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

  // select January
  await page.locator('[data-test-id="dp-input"]').click();
  await page.getByText("Jan").click();
  await page.locator('[data-test-id="select-button"]').click();

  // check target date
  await expect(page.locator("[data-test-id='dp-input']")).toHaveValue(
    `${currentYear}-01-01`
  );

  // input overview
  await page
    .locator("div")
    .filter({ hasText: /^Overview$/ })
    .click();
  await page
    .getByRole("spinbutton", { name: "Gross Income", exact: true })
    .fill(testData[0].overview.grossIncome.toString());
  await page
    .getByRole("spinbutton", { name: "Net Income", exact: true })
    .fill(testData[0].overview.netIncome.toString());
  await page
    .getByRole("spinbutton", { name: "Operating Time", exact: true })
    .fill(testData[0].overview.operatingTime.toString());
  await page
    .getByRole("spinbutton", { name: "Overtime", exact: true })
    .fill(testData[0].overview.overtime.toString());
  await page
    .getByRole("spinbutton", { name: "Bonus", exact: true })
    .fill(testData[0].overview.bonus.toString());
  await page
    .getByRole("spinbutton", { name: "Bonus Take Home", exact: true })
    .fill(testData[0].overview.bonusTakeHome.toString());

  // input structure
  await page
    .locator("div")
    .filter({ hasText: /^Structure$/ })
    .click();
  await page
    .getByRole("spinbutton", { name: "Basic Salary", exact: true })
    .fill(testData[0].structure.basicSalary.toString());
  await page
    .getByRole("spinbutton", { name: "Overtime Pay", exact: true })
    .fill(testData[0].structure.overtimePay.toString());
  await page
    .getByRole("spinbutton", { name: "Housing Allowance", exact: true })
    .fill(testData[0].structure.housingAllowance.toString());
  await page
    .getByRole("spinbutton", { name: "Positional Allowance", exact: true })
    .fill(testData[0].structure.positionAllowance.toString());
  await page
    .getByRole("spinbutton", { name: "Other", exact: true })
    .fill(testData[0].structure.other.toString());

  // input payslip data
  for (const category of testData[0].payslipData) {
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

  // input dummy payslip data
  await page.getByRole("button", { name: "Add Category" }).click();
  await page.getByRole("textbox", { name: "*" }).fill("dummy");
  await page.getByRole("button", { name: "OK" }).click();
  await page
    .locator("div")
    .filter({ hasText: /^dummy$/ })
    .first()
    .click();
  await page.getByRole("button", { name: "Add Payslip" }).last().click();
  await page.getByRole("textbox", { name: "*" }).fill("dummy1");
  await page.getByRole("button", { name: "OK" }).click();
  await page.getByRole("spinbutton", { name: "dummy1" }).fill("111");
  await page.getByRole("button", { name: "Add Payslip" }).last().click();
  await page.getByRole("textbox", { name: "*" }).fill("dummy2");
  await page.getByRole("button", { name: "OK" }).click();
  await page.getByRole("spinbutton", { name: "dummy2" }).fill("222");

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

  // delete salary data
  await page.getByRole("button", { name: "Delete", exact: true }).click();
  await page.getByRole("button", { name: "Yes", exact: true }).click();
  await page.getByRole("button", { name: "OK", exact: true }).click();
});
