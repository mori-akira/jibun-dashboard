import { test, expect } from "@playwright/test";

test("test common layout", async ({ page }) => {
  const userName = process.env.E2E_USERNAME;
  if (!userName) {
    throw new Error("E2E_USERNAME is not set");
  }

  // access to top
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check title
  await expect(page.getByTestId("app-header-title")).toHaveText(
    "Jibun Dashboard"
  );

  // check username
  await expect(page.getByRole("banner").getByText(userName)).toBeVisible();

  // check email
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
