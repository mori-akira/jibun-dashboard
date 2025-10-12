import { test, expect } from "@playwright/test";

test("check logout", async ({ page }) => {
  const cognitoDomain = process.env.E2E_COGNITO_DOMAIN;
  if (!cognitoDomain) {
    throw new Error("E2E_COGNITO_DOMAIN is not set");
  }

  const userName = process.env.E2E_USERNAME;
  if (!userName) {
    throw new Error("E2E_USERNAME is not set");
  }

  // access to top
  await page.goto("/", { waitUntil: "domcontentloaded" });

  // logout
  await openSubMenu(page, userName);
  await Promise.all([
    page.waitForURL("**/logout"),
    page.getByRole("link", { name: "Logout" }).click(),
  ]);
  await page.waitForURL("**/login**");

  // check domain
  const current = new URL(page.url());
  expect(current.host).toBe(cognitoDomain);
});

const openSubMenu = async (page: any, userName: string) => {
  await page
    .getByRole("banner")
    .locator("div")
    .filter({ hasText: userName })
    .locator("div")
    .click();
};
