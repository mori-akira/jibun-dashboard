import { test, expect } from "@playwright/test";
import { openSubMenu } from "../helpers/ui";

test("test shared-links function", async ({ page, browser }) => {
  const userName = process.env.E2E_USERNAME;
  if (!userName) {
    throw new Error("E2E_USERNAME is not set");
  }

  // navigate to shared-links page
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
  await openSubMenu(page, userName);
  await Promise.all([
    page.waitForURL("**/profile/shared-links"),
    page.getByRole("link", { name: "Shared Links" }).click(),
  ]);

  // open add modal
  await page.getByRole("button", { name: "Add New One" }).click();

  // select data type: Vocabulary
  await page.getByRole('dialog').getByText('Qualification').click();

  // set expiry date (1 year from now)
  const currentDate = new Date().toISOString().split("T")[0];
  page.locator('[data-test-id="dp-input"]').click();
  await page.locator(`[data-test-id="dp-${currentDate}"]`).click();
  await page.locator('[data-test-id="select-button"]').click();

  // execute
  await page.getByRole("button", { name: "Execute", exact: true }).click();
  await page.getByRole("button", { name: "OK", exact: true }).click();

  // verify link appears in table
  await expect(page.getByText("vocabulary")).toBeVisible();
  await expect(page.getByText("active")).toBeVisible();

  // verify Shared Links summary on PC home
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
  await expect(
    page.getByRole("main").getByText("Shared Links", { exact: true })
  ).toBeVisible();
  await expect(page.locator("span.bg-green-600").getByText("active")).toBeVisible();

  // verify Shared Links summary on mobile home
  await page.goto("/m", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
  await expect(
    page.getByRole("main").getByText("Shared Links", { exact: true })
  ).toBeVisible();
  await expect(page.locator("span.bg-green-600").getByText("active")).toBeVisible();

  // back to shared-links page to get share URL
  await page.goto("/profile/shared-links", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
  const shareUrlLink = page.locator("a[href*='/share/']").first();
  const shareUrl = await shareUrlLink.getAttribute("href");
  expect(shareUrl).toBeTruthy();

  // navigate to share URL (authenticated)
  await page.goto(shareUrl!, { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // verify share URL is accessible without auth (new browser context)
  const unauthContext = await browser.newContext();
  const unauthPage = await unauthContext.newPage();
  await unauthPage.goto(shareUrl!, { waitUntil: "domcontentloaded" });
  await unauthPage.waitForLoadState("networkidle");

  const currentUrl = new URL(unauthPage.url());
  const cognitoDomain = process.env.E2E_COGNITO_DOMAIN ?? "";
  expect(currentUrl.host).not.toBe(cognitoDomain);
  expect(currentUrl.pathname).toMatch(/^\/share\//);
  await unauthContext.close();

  // back to shared-links page
  await page.goto("/profile/shared-links", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // delete the shared link
  await page.locator("table tbody tr").first().locator("span.iconify").click();
  await page.getByRole("button", { name: "Yes", exact: true }).click();

  // verify link is removed
  await page.waitForLoadState("networkidle");
  await expect(page.getByText("No Item")).toBeVisible();
});
