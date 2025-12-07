import { test, expect } from "@playwright/test";

import { openSubMenu, openMobileMenu } from "../helpers/ui";

test("test navigation", async ({ page }) => {
  const userName = process.env.E2E_USERNAME;
  if (!userName) {
    throw new Error("E2E_USERNAME is not set");
  }

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
    page.waitForURL(""),
    page.getByRole("link", { name: "Home" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Home", { exact: true })
  ).toBeVisible();

  // edit profile
  await openSubMenu(page, userName);
  await Promise.all([
    page.waitForURL("**/profile/edit"),
    page.getByRole("link", { name: "Edit Profile" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Edit Profile", { exact: true })
  ).toBeVisible();

  // change password
  await openSubMenu(page, userName);
  await Promise.all([
    page.waitForURL("**/profile/change-password"),
    page.getByRole("link", { name: "Change Password" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Change Password", { exact: true })
  ).toBeVisible();

  // setting
  await openSubMenu(page, userName);
  await Promise.all([
    page.waitForURL("**/setting"),
    page.getByRole("link", { name: "Setting" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Setting", { exact: true })
  ).toBeVisible();

  // top
  await Promise.all([
    page.waitForURL(""),
    page.getByTestId("app-header-title").click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Home", { exact: true })
  ).toBeVisible();

  // mobile top
  await page.goto("/m", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // salary
  await openMobileMenu(page);
  await Promise.all([
    page.waitForURL("**/salary"),
    page.getByRole("link", { name: "Salary" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Salary", { exact: true })
  ).toBeVisible();

  // qualification
  await openMobileMenu(page);
  await Promise.all([
    page.waitForURL("**/qualification"),
    page.getByRole("link", { name: "Qualification" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Qualification", { exact: true })
  ).toBeVisible();

  // home
  await openMobileMenu(page);
  await Promise.all([
    page.waitForURL(""),
    page.getByRole("link", { name: "Home" }).click(),
  ]);
  await expect(
    page.getByRole("main").getByText("Home", { exact: true })
  ).toBeVisible();
});
