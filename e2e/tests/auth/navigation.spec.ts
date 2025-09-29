import { test, expect } from "@playwright/test";

test("check navigation", async ({ page }) => {
  // access to top
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // access to salary
  await Promise.all([
    page.waitForURL("**/salary"),
    page.getByRole("link", { name: "Salary" }).click(),
  ]);
  await expect(page.getByRole("main").getByText("Salary")).toBeVisible();

  // access to qualification
  await Promise.all([
    page.waitForURL("**/qualification"),
    page.getByRole("link", { name: "Qualification" }).click(),
  ]);
  await expect(page.getByRole("main").getByText("Qualification")).toBeVisible();
});
