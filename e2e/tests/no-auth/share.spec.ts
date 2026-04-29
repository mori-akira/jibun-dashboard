import { test, expect } from "@playwright/test";

test("share page route does not require authentication", async ({ page }) => {
  const cognitoDomain = process.env.E2E_COGNITO_DOMAIN;
  if (!cognitoDomain) {
    throw new Error("E2E_COGNITO_DOMAIN is not set");
  }

  await page.goto("/share/00000000-0000-0000-0000-000000000000", {
    waitUntil: "domcontentloaded",
  });
  await page.waitForLoadState("networkidle");

  // should NOT be redirected to Cognito
  const current = new URL(page.url());
  expect(current.host).not.toBe(cognitoDomain);
  expect(current.pathname).toMatch(/^\/share\//);
});
