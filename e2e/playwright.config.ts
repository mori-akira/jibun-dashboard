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
      name: "auth",
      dependencies: ["setup"],
      testMatch: /\/auth\/.+\.spec\.ts$/,
      use: { ...devices["Desktop Chrome"], storageState: STORAGE_STATE },
    },
  ],
  reporter: [
    ["list"],
    ["html", { outputFolder: ".playwright/playwright-report", open: "never" }],
  ],
});
