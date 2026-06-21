import { test as base, type Page } from "@playwright/test";

export { expect } from "@playwright/test";

// Hardened `page.goto` for the authenticated suite.
//
// The app's auth middleware (front/middleware/auth.global.ts) aborts the
// current navigation and kicks off a Cognito SSO re-login when a *transient*
// `signinSilent` failure momentarily makes `isAuthenticated()` return false.
// Playwright surfaces that as `page.goto: net::ERR_ABORTED`. The Cognito SSO
// session is still valid, so the redirect resumes to the requested URL on its
// own (the middleware passes the original path as the return path).
//
// We only intervene on that specific error: wait for the SSO round-trip to
// settle, then re-navigate if we did not land on the target. Successful
// navigations never reach the catch branch, so this cannot turn a passing
// navigation into a failing one.
const hardenedGoto = (page: Page): Page["goto"] => {
  const originalGoto = page.goto.bind(page);
  return async (url, options) => {
    try {
      return await originalGoto(url, options);
    } catch (error) {
      if (!String(error).includes("ERR_ABORTED")) {
        throw error;
      }
      await page.waitForLoadState("networkidle").catch(() => {});
      if (!page.url().includes(String(url))) {
        return await originalGoto(url, options);
      }
      return null;
    }
  };
};

export const test = base.extend({
  page: async ({ page }, use) => {
    page.goto = hardenedGoto(page);
    await use(page);
  },
});
