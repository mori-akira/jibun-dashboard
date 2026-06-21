import { test, expect } from "../_fixtures";
import type { Page } from "@playwright/test";
import testData from "./cardbook.spec.data.json";

test("test cardbook function", async ({ page }) => {
  // === PC: cardbook list is empty ===
  await page.goto("/cardbook", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  await expect(page.getByText("No Item")).toBeVisible();

  // === PC: create new cardbook ===
  await page.getByRole("button", { name: "New Book" }).click();
  await page.getByRole("dialog").getByRole("textbox").fill(testData.cardbookName);
  await page.getByRole("button", { name: "OK", exact: true }).click();
  await page.waitForLoadState("networkidle");

  // capture cardbookId from URL
  await page.waitForURL(/\/cardbook\/.+/);
  const cardbookId =
    page.url().match(/\/cardbook\/([a-f0-9-]+)/)?.[1] ?? "";
  expect(cardbookId).not.toBe("");

  // === PC: add 3 cards ===
  for (const card of testData.cards) {
    await addCard(page, card);
  }

  // verify 3 cards in table
  for (const card of testData.cards) {
    await expect(
      page.getByRole("cell", { name: card.front, exact: true })
    ).toBeVisible();
  }

  // === PC quiz: 3 cards, 2 correct, 1 incorrect ===
  await page.getByRole("button", { name: "Quiz" }).click();
  await page.waitForLoadState("networkidle");

  await expect(page.getByText("/ 3 in scope")).toBeVisible();
  await page.getByRole("button", { name: "Start Quiz" }).click();

  const pcAnswers = ["Correct", "Correct", "Incorrect"] as const;
  for (let i = 0; i < pcAnswers.length; i++) {
    await page.locator(".flashcard").click();
    await page.getByRole("button", { name: pcAnswers[i], exact: true }).click();
    if (i < pcAnswers.length - 1) {
      await page.getByRole("button", { name: "Next" }).click();
    } else {
      await page.getByRole("button", { name: "Complete" }).click();
    }
  }

  await checkQuizResult(page, 2, 3);
  await page.getByRole("button", { name: "Submit" }).click();
  await page.waitForLoadState("networkidle");

  // === PC quiz history ===
  await page.waitForURL(/\/cardbook\/.+\/quiz\/history/);
  await expect(page.locator("tr")).toHaveCount(2); // 1 header + 1 data row
  // columns: 0=answeredAt, 1=questions, 2=direction, 3=correct, 4=incorrect
  await expect(page.locator("tr").nth(1).locator("td").nth(1)).toHaveText("3");
  await expect(page.locator("tr").nth(1).locator("td").nth(3)).toHaveText("2");
  await expect(page.locator("tr").nth(1).locator("td").nth(4)).toHaveText("1");

  // === mobile: cardbook list ===
  await page.goto("/m/cardbook", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  await expect(
    page.getByRole("cell", { name: testData.cardbookName })
  ).toBeVisible();

  // navigate to card list
  await page.getByRole("row", { name: testData.cardbookName }).click();
  await page.waitForLoadState("networkidle");

  // verify 3 original cards
  for (const card of testData.cards) {
    await expect(
      page.getByRole("cell", { name: card.front, exact: true })
    ).toBeVisible();
  }

  // === mobile: add term-only card ===
  await page.getByRole("button", { name: "Add" }).first().click();
  await page.getByRole("dialog").waitFor({ state: "visible" });
  const mobileTermInput = page
    .getByRole("dialog")
    .getByRole("textbox", { name: "Term" });
  await mobileTermInput.fill(testData.mobileCardTerm);
  await mobileTermInput.press("Tab");
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Add", exact: true })
    .click();
  await page.waitForLoadState("networkidle");

  // verify 4th card appears
  await expect(
    page.getByRole("cell", { name: testData.mobileCardTerm, exact: true })
  ).toBeVisible();

  // === mobile quiz: 4 cards, all correct ===
  await page.goto(`/m/cardbook/${cardbookId}/quiz`, {
    waitUntil: "domcontentloaded",
  });
  await page.waitForLoadState("networkidle");

  await expect(page.getByText("/ 4 in scope")).toBeVisible();
  await page.getByRole("button", { name: "Start Quiz" }).click();

  for (let i = 0; i < 4; i++) {
    await page.locator(".flashcard").click();
    await page
      .getByRole("button", { name: "Correct", exact: true })
      .click();
    if (i < 3) {
      await page.getByRole("button", { name: "Next" }).click();
    } else {
      await page.getByRole("button", { name: "Complete" }).click();
    }
  }

  await checkQuizResult(page, 4, 4);
  await page.getByRole("button", { name: "Submit" }).click();
  await page.waitForLoadState("networkidle");

  // === mobile quiz history ===
  await page.waitForURL(/\/m\/cardbook\/.+\/quiz\/history/);
  await expect(page.locator("tr")).toHaveCount(3); // 1 header + 2 data rows
  // visible columns: 0=answeredAt, 1=questions, 2=correct (sorted newest first)
  await expect(page.locator("tr").nth(1).locator("td").nth(1)).toHaveText("4");
  await expect(page.locator("tr").nth(1).locator("td").nth(2)).toHaveText("4");
  await expect(page.locator("tr").nth(2).locator("td").nth(1)).toHaveText("3");
  await expect(page.locator("tr").nth(2).locator("td").nth(2)).toHaveText("2");

  // === cleanup: delete all cards ===
  await page.goto(`/cardbook/${cardbookId}`, { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // select all via header checkbox
  await page.locator(".iconify.i-tabler\\:square-dashed").first().click();
  await page.getByRole("button", { name: "Delete All" }).click();
  await expect(
    page.getByText("Confirm deletion of all checked cards?")
  ).toBeVisible();
  await page.getByRole("button", { name: "Yes" }).click();
  await page.waitForLoadState("networkidle");
  await expect(page.getByText("No Item")).toBeVisible();

  // === cleanup: delete cardbook ===
  await page.goto("/cardbook", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  await page
    .locator("tr:nth-child(1)")
    .locator(".iconify.i-tabler\\:trash")
    .click();
  await page.waitForLoadState("networkidle");
  await expect(page.getByText("No Item")).toBeVisible();
});

type Card = { front: string; back?: string };

const addCard = async (page: Page, card: Card) => {
  await page.getByRole("button", { name: "Add Card" }).click();
  await page.getByRole("dialog").waitFor({ state: "visible" });
  const termInput = page
    .getByRole("dialog")
    .getByRole("textbox", { name: "Term" });
  const descInput = page
    .getByRole("dialog")
    .getByRole("textbox", { name: "Description" });
  await termInput.fill(card.front);
  await termInput.press("Tab");
  await descInput.fill(card.back ?? "");
  await descInput.press("Tab");
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Add", exact: true })
    .click();
  await page.waitForLoadState("networkidle");
};

const checkQuizResult = async (
  page: Page,
  correct: number,
  total: number
) => {
  const correctLabel = page.locator("p").filter({ hasText: /^Correct$/ });
  await expect(
    correctLabel.locator("xpath=preceding-sibling::p")
  ).toHaveText(String(correct));
  const totalLabel = page.locator("p").filter({ hasText: /^Total$/ });
  await expect(
    totalLabel.locator("xpath=preceding-sibling::p")
  ).toHaveText(String(total));
};
