import * as dotenv from "dotenv";
import path from "path";
import { defineConfig, devices } from "@playwright/test";

dotenv.config({ path: ".env" });
const baseURL = process.env.E2E_APP_URL ?? "http://localhost:3000";
const headless = process.env.E2E_CI === "true" ? true : false;
const STORAGE_STATE = path.resolve(__dirname, ".playwright/auth/user.json");

export default defineConfig({
  testDir: path.resolve(__dirname, "./tests"),
  outputDir: path.resolve(__dirname, ".playwright/test-results"),
  timeout: 30000,
  expect: { timeout: 10000 },
  workers: headless ? undefined : 1,
  fullyParallel: headless ? true : false,
  retries: headless ? 2 : 0,
  use: {
    baseURL,
    trace: "on-first-retry",
    headless,
  },
  projects: [
    {
      name: "no-auth",
      testMatch: /\/no-auth\/.+\.spec\.ts$/,
      use: { ...devices["Desktop Chrome"] },
    },
    {
      name: "setup",
      testMatch: /\/setup\/.+\.spec\.ts$/,
      use: { ...devices["Desktop Chrome"] },
    },
    {
      name: "seed",
      dependencies: ["setup"],
      testMatch: /\/seed\/.+\.spec\.ts$/,
      use: { ...devices["Desktop Chrome"], storageState: STORAGE_STATE },
    },
    {
      name: "auth",
      dependencies: ["seed"],
      testMatch: /\/auth\/.+\.spec\.ts$/,
      use: { ...devices["Desktop Chrome"], storageState: STORAGE_STATE },
    },
    {
      name: "teardown",
      dependencies: ["setup"],
      testMatch: /\/teardown\/.+\.spec\.ts$/,
      use: { ...devices["Desktop Chrome"], storageState: STORAGE_STATE },
    },
  ],
  reporter: [
    ["list"],
    ["html", { outputFolder: ".playwright/playwright-report", open: "never" }],
  ],
});
