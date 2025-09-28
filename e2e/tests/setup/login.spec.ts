import { test, expect } from "@playwright/test";

const STORAGE_STATE = ".playwright/auth/user.json";

test("login once and save storageState", async ({ page }) => {
  const cognitoDomain = process.env.E2E_COGNITO_DOMAIN;
  const username = process.env.E2E_USERNAME;
  const password = process.env.E2E_PASSWORD;

  if (!cognitoDomain) {
    throw new Error("E2E_COGNITO_DOMAIN is not set");
  }
  if (!username || !password) {
    throw new Error("E2E_USERNAME/PASSWORD are not set");
  }

  // top → Cognito Login UI
  await page.goto("/", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check domain
  const current = new URL(page.url());
  expect(current.host).toBe(cognitoDomain);

  // input
  await page.fill(
    'input[name="username"]:visible, #signInFormUsername:visible',
    username
  );
  await page.fill(
    'input[name="password"]:visible, #signInFormPassword:visible',
    password
  );

  // login
  await page.click(
    'button[type="submit"]:visible, input[type="submit"]:visible'
  );
  await page.waitForLoadState("networkidle");

  // check title
  await expect(page.getByTestId("app-header-title")).toHaveText(
    "Jibun Dashboard"
  );

  // save auth state
  await page.context().storageState({ path: STORAGE_STATE });
});
