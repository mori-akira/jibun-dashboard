import { test, expect } from "@playwright/test";

test("check common layout", async ({ page }) => {
  const username = process.env.E2E_USERNAME;
  if (!username) {
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
  await expect(page.getByTestId("app-header-username")).toHaveText(username);
});
