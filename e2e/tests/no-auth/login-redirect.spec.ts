import { test, expect } from "@playwright/test";

test("non-auth user is redirected to Cognito login", async ({ page }) => {
  const cognitoDomain = process.env.E2E_COGNITO_DOMAIN;
  if (!cognitoDomain) {
    throw new Error("E2E_COGNITO_DOMAIN is not set");
  }

  // access to top
  await page.goto("/", { waitUntil: "domcontentloaded" });

  // wait
  await page.waitForLoadState("networkidle");

  // check domain
  const current = new URL(page.url());
  expect(current.host).toBe(cognitoDomain);

  // check path
  expect(current.pathname).toMatch(/\/(login|oauth2\/authorize)/);
});
