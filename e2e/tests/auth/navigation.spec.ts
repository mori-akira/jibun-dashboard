import { test, expect } from "@playwright/test";

test("check navigation", async ({ page }) => {
  // top
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // salary
  await Promise.all([
    page.waitForURL("**/salary"),
    page.getByRole("link", { name: "Salary" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Salary", { exact: true })
  ).toBeVisible();

  // qualification
  await Promise.all([
    page.waitForURL("**/qualification"),
    page.getByRole("link", { name: "Qualification" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Qualification", { exact: true })
  ).toBeVisible();

  // vocabulary
  await Promise.all([
    page.waitForURL("**/vocabulary"),
    page.getByRole("link", { name: "Vocabulary" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Vocabulary", { exact: true })
  ).toBeVisible();

  // financial asset
  await Promise.all([
    page.waitForURL("**/financial-asset"),
    page.getByRole("link", { name: "Financial Asset" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Financial Asset", { exact: true })
  ).toBeVisible();

  // study plan
  await Promise.all([
    page.waitForURL("**/study-plan"),
    page.getByRole("link", { name: "Study Plan" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Study Plan", { exact: true })
  ).toBeVisible();

  // home
  await Promise.all([
    page.waitForURL("**/"),
    page.getByRole("link", { name: "Home" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Homeee", { exact: true })
  ).toBeVisible();
});
