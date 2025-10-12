import { test, expect } from "@playwright/test";

test("test profile function", async ({ page }) => {
  const userName = process.env.E2E_USERNAME;
  if (!userName) {
    throw new Error("E2E_USERNAME is not set");
  }

  // access to top
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // access to edit profile
  await openSubMenu(page, userName);
  await Promise.all([
    page.waitForURL("**/profile/edit"),
    page.getByRole("link", { name: "Edit Profile" }).click(),
  ]);

  // input
  await page.getByRole("textbox", { name: "Name" }).fill("user-name");
  await page.getByRole("textbox", { name: "Email" }).fill("test2@example.com");

  // execute
  await page.getByRole("button", { name: "Execute", exact: true }).click();
  await page.getByRole("button", { name: "OK", exact: true }).click();
  await page.waitForURL("");

  // check user name
  await expect(page.getByRole("banner").getByText("user-name")).toBeVisible();

  // check email
  await openSubMenu(page, "user-name");
  await expect(page.getByText("test2@example.com")).toBeVisible();

  // revert
  await Promise.all([
    page.waitForURL("**/profile/edit"),
    page.getByRole("link", { name: "Edit Profile" }).click(),
  ]);
  await page.getByRole("textbox", { name: "Name" }).fill(userName);
  await page.getByRole("textbox", { name: "Email" }).fill(userName);
  await page.getByRole("button", { name: "Execute", exact: true }).click();
  await page.getByRole("button", { name: "OK", exact: true }).click();
  await page.waitForURL("");

  // check
  await expect(page.getByRole("banner").getByText(userName)).toBeVisible();
  await openSubMenu(page, userName);
  await expect(page.getByText(userName).nth(1)).toBeVisible();
});

const openSubMenu = async (page: any, userName: string) => {
  await page
    .getByRole("banner")
    .locator("div")
    .filter({ hasText: userName })
    .locator("div")
    .click();
};
