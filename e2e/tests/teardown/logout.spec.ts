import { test, expect } from "@playwright/test";

test("check logout", async ({ page }) => {
  const cognitoDomain = process.env.E2E_COGNITO_DOMAIN;
  if (!cognitoDomain) {
    throw new Error("E2E_COGNITO_DOMAIN is not set");
  }

  // access to top
  await page.goto("/", { waitUntil: "domcontentloaded" });

  // logout
  await openSubMenu(page);
  await Promise.all([
    page.waitForURL("**/logout"),
    page.getByRole("link", { name: "Logout" }).click(),
  ]);
  await page.waitForURL("**/login**");

  // check domain
  const current = new URL(page.url());
  expect(current.host).toBe(cognitoDomain);
});

const openSubMenu = async (page: any) => {
  await page
    .getByRole("banner")
    .locator("div")
    .filter({ hasText: "test@example.com" })
    .locator("div")
    .click();
};
