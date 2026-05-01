import { test, expect, Page } from "@playwright/test";
import testData from "./vocabulary.spec.data.json";

test("test vocabulary function", async ({ page }) => {
  // access to vocabulary
  await page.goto("/vocabulary", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check vocabulary list is empty
  await expect(page.getByText("No Item")).toBeVisible();

  // === add vocabulary tags ===
  await page.getByRole("button", { name: "Manage Tags" }).click();

  // add vocabulary tags
  for (const tag of testData.vocabularyTags) {
    await addVocabularyTag(page, tag as VocabularyTag);
    await page.waitForTimeout(100);
  }

  // check added vocabulary tags
  for (const index in testData.vocabularyTags) {
    const tag: VocabularyTag = testData.vocabularyTags[index] as VocabularyTag;
    await checkVocabularyTagDisplay(page, tag, Number(index) + 1);
  }

  // === add vocabularies ===
  // access to edit
  await page.getByRole('main').getByRole('link', { name: 'Vocabulary' }).click();
  await page.getByRole("button", { name: "Edit" }).click();

  // add vocabularies
  for (const vocabulary of testData.vocabularies) {
    await addVocabulary(page, vocabulary as Vocabulary);
    await page.waitForTimeout(100);
  }

  // check added vocabularies
  for (const index in testData.vocabularies) {
    const vocabulary: Vocabulary = testData.vocabularies[index] as Vocabulary;
    await checkVocabularyDisplay(page, vocabulary, testData.vocabularies.length - Number(index), 1);
  }

  // add dummy vocabulary
  const dummyVocabulary: Vocabulary = {
    name: "Dummy Vocabulary",
    description: "This is a dummy vocabulary for testing.",
    tags: [],
  };
  await addVocabulary(page, dummyVocabulary);
  await checkVocabularyDisplay(page, dummyVocabulary, 1, 1);

  // delete dummy vocabulary
  await page.locator("tr:nth-child(1) > td > .cursor-pointer > .iconify").click();
  await page.getByRole("button", { name: "Delete All" }).click();
  await expect(
    page.getByText("Confirm deletion of all checked vocabularies?")
  ).toBeVisible();
  await page.getByRole("button", { name: "Yes" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();
  await expect(page.getByText("Dummy Vocabulary")).toHaveCount(0);

  // === list vocabularies ===
  await page
    .getByRole("main")
    .getByRole("link", { name: "Vocabulary" })
    .click();

  // check listed vocabularies
  for (const index in testData.vocabularies) {
    const vocabulary: Vocabulary = testData.vocabularies[index] as Vocabulary;
    await checkVocabularyDisplay(page, vocabulary, testData.vocabularies.length - Number(index));
  }

  // === home (summary) ===
  await page.getByTestId("app-header-title").click();

  // check vocabulary summary
  await checkTagCountSummaryDisplay(page);

  // === mobile home (summary) ===
  await page.goto("/m", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check vocabulary summary
  await checkTagCountSummaryDisplay(page);

  // === mobile vocabulary list ===
  await page.goto("/m/vocabulary", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check listed vocabularies
  for (const index in testData.vocabularies) {
    const vocabulary: Vocabulary = testData.vocabularies[index] as Vocabulary;
    await checkVocabularyDisplay(page, vocabulary, testData.vocabularies.length - Number(index));
  }

  // === mobile vocabulary add ===
  // add vocabulary with name only
  const mobileAddName = "Mobile Test Vocabulary";
  await addMobileVocabulary(page, mobileAddName);
  await page.waitForLoadState("networkidle");

  // check added vocabulary
  await checkVocabularyDisplay(
    page,
    { name: mobileAddName, tags: [] },
    1,
  );

  // === PC Quiz: no tag filter, 5 questions, 3 correct, 2 incorrect ===
  // 5 original vocabularies + 1 mobile test = 6 in scope with no filter
  await page.goto("/vocabulary/quiz", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  await expect(page.getByText("/ 6 in scope")).toBeVisible();

  await page.locator("select").selectOption("5");
  await page.getByRole("button", { name: "Start Quiz" }).click();

  const pcVocabularies: VocabularyPair[] = [
    ...testData.vocabularies.map((v) => ({ name: v.name, description: v.description })),
    { name: mobileAddName, description: "" },
  ];
  const pcAnswers = ["Correct", "Correct", "Correct", "Incorrect", "Incorrect"] as const;
  for (let i = 0; i < pcAnswers.length; i++) {
    await checkFlashcard(page, pcVocabularies);
    await page.getByRole("button", { name: pcAnswers[i], exact: true }).click();
    if (i < pcAnswers.length - 1) {
      await page.getByRole("button", { name: "Next" }).click();
    } else {
      await page.getByRole("button", { name: "Complete" }).click();
    }
  }

  await checkQuizResult(page, 3, 5);
  await page.getByRole("button", { name: "Submit" }).click();
  await page.waitForLoadState("networkidle");

  // check PC quiz history
  await page.waitForURL(/\/vocabulary\/quiz\/history/);
  await expect(page.locator("tr")).toHaveCount(2);
  // td indices: 0=answeredAt, 1=tags, 2=questionCount, 3=direction, 4=correctCount, 5=incorrectCount
  await expect(page.locator("tr").nth(1).locator("td").nth(1)).toContainText("All");
  await expect(page.locator("tr").nth(1).locator("td").nth(2)).toHaveText("5");
  await expect(page.locator("tr").nth(1).locator("td").nth(4)).toHaveText("3");
  await expect(page.locator("tr").nth(1).locator("td").nth(5)).toHaveText("2");

  // === Mobile Quiz: IT + 読書 tags, 4 questions, all correct ===
  // アジャイル(IT) + PDCA(IT) + フィードバックループ(IT) + アンカリング効果(読書) = 4 in scope
  await page.goto("/m/vocabulary/quiz", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  await page.getByRole("button", { name: "IT", exact: true }).click();
  await page.getByRole("button", { name: "読書", exact: true }).click();

  await expect(page.getByText("/ 4 in scope")).toBeVisible();

  await page.locator('input[type="radio"][value="BACK_TO_FRONT"]').click();

  await page.getByRole("button", { name: "Start Quiz" }).click();

  const mobileQuizVocabularies: VocabularyPair[] = testData.vocabularies
    .filter((v) => v.tags.some((t) => ["IT", "読書"].includes(t)))
    .map((v) => ({ name: v.name, description: v.description }));
  for (let i = 0; i < 4; i++) {
    await checkFlashcard(page, mobileQuizVocabularies);
    await page.getByRole("button", { name: "Correct", exact: true }).click();
    if (i < 3) {
      await page.getByRole("button", { name: "Next" }).click();
    } else {
      await page.getByRole("button", { name: "Complete" }).click();
    }
  }

  await checkQuizResult(page, 4, 4);
  await page.getByRole("button", { name: "Submit" }).click();
  await page.waitForLoadState("networkidle");

  // check mobile quiz history
  await page.waitForURL(/\/m\/vocabulary\/quiz\/history/);
  await expect(page.locator("tr")).toHaveCount(3); // 1 header + 2 data rows
  // Row 1 = most recent (mobile quiz), sorted by answeredAt desc
  // Mobile visible fields: 0=answeredAt, 1=tags, 2=questionCount, 3=correctCount
  await expect(page.locator("tr").nth(1).locator("td").nth(1)).toContainText("IT");
  await expect(page.locator("tr").nth(1).locator("td").nth(1)).toContainText("読書");
  await expect(page.locator("tr").nth(1).locator("td").nth(2)).toHaveText("4");
  await expect(page.locator("tr").nth(1).locator("td").nth(3)).toHaveText("4");

  // === delete all vocabularies ===
  await page.goto("/vocabulary/edit", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // select all and delete
  await page.locator(".iconify.i-tabler\\:square-dashed").first().click();
  await page.getByRole("button", { name: "Delete All" }).click();
  await expect(
    page.getByText("Confirm deletion of all checked vocabularies?")
  ).toBeVisible();
  await page.getByRole("button", { name: "Yes" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();

  // check vocabulary list is empty
  await expect(page.getByText("No Item")).toBeVisible();

  // === delete all vocabulary tags ===
  await page.goto("/vocabulary/tags", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // delete one by one (first row always shifts up after deletion)
  for (let i = 0; i < testData.vocabularyTags.length; i++) {
    await page
      .locator("tr:nth-child(1)")
      .locator(".iconify.i-tabler\\:trash")
      .click();
    await expect(page.getByText("Confirm deletion of this tag?")).toBeVisible();
    await page.getByRole("button", { name: "Yes" }).click();
    await expect(page.getByText("Process completed successfully")).toBeVisible();
    await page.getByRole("button", { name: "OK" }).click();
  }

  // check vocabulary tag list is empty
  await expect(page.getByText("No Item")).toBeVisible();
});

type VocabularyTag = {
  vocabularyTag: string;
};

type Vocabulary = {
  name: string;
  description?: string;
  tags: string[];
};

const addVocabularyTag = async (page: Page, tag: VocabularyTag) => {
  // open modal
  await page.getByRole("button", { name: "Add New One" }).click();
  await page.waitForTimeout(100);
  // tag name
  await page
    .locator("label:has-text('Tag') + div > input")
    .last()
    .fill(tag.vocabularyTag);
  // execute
  await page.getByRole("button", { name: "Execute" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();
};

const addVocabulary = async (page: Page, vocabulary: Vocabulary) => {
  // open modal
  await page.getByRole("button", { name: "Add New One" }).click();
  await page.waitForTimeout(100);
  // name
  await page
    .locator("label:has-text('Name') + div > input")
    .last()
    .fill(vocabulary.name);
  // description
  await page
    .locator("label:has-text('Description') + div > textarea")
    .last()
    .fill(vocabulary.description ?? "");
  // tags (click badge by exact name match)
  for (const tag of vocabulary.tags) {
    await page
      .locator("label:has-text('Tags') + div > div")
      .filter({ hasText: new RegExp(`^${tag}$`) })
      .click();
  }
  // execute
  await page.getByRole("button", { name: "Execute" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();
};

const checkVocabularyTagDisplay = async (
  page: Page,
  tag: VocabularyTag,
  row: number
) => {
  // row number
  await expect(
    page.locator("tr").nth(row).locator("td").nth(0)
  ).toHaveText(row + "");
  // tag name
  await expect(
    page.locator("tr").nth(row).locator("td").nth(1)
  ).toHaveText(tag.vocabularyTag);
};

const addMobileVocabulary = async (page: Page, name: string) => {
  // open add modal
  await page.getByRole("button", { name: "Add" }).first().click();
  await page.waitForTimeout(100);
  // name
  await page
    .locator("label:has-text('Name') + div > input")
    .last()
    .fill(name);
  // submit (last "Add" button is the one inside the modal)
  await page.getByRole("button", { name: "Add" }).last().click();
};

const checkTagCountSummaryDisplay = async (page: Page) => {
  // top 3 tags from test data
  const expectedTags = [
    { tag: "IT", count: 3 },
    { tag: "心理学", count: 2 },
    { tag: "経済", count: 1 },
  ];
  for (const item of expectedTags) {
    const row = page.locator("div.flex.items-center.gap-2", {
      has: page.getByText(item.tag, { exact: true }),
    });
    await expect(row.locator("span").last()).toHaveText(item.count + "");
  }
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

type VocabularyPair = {
  name: string;
  description: string;
};

const checkFlashcard = async (page: Page, vocabularies: VocabularyPair[]) => {
  const frontLabel = (
    await page
      .locator(".flashcard-front .flashcard-content p.font-cursive")
      .textContent()
  )?.trim() ?? "";
  const frontContent = (
    await page
      .locator(".flashcard-front .flashcard-content p.whitespace-pre-wrap")
      .textContent()
  )?.trim() ?? "";

  const isNameOnFront = frontLabel === "Name";
  const vocab = vocabularies.find((v) =>
    isNameOnFront ? v.name === frontContent : v.description === frontContent
  );
  expect(vocab).toBeDefined();

  await page.locator(".flashcard").click();

  const backLabel = (
    await page
      .locator(".flashcard-back .flashcard-content p.font-cursive")
      .textContent()
  )?.trim() ?? "";
  const backContent = (
    await page
      .locator(".flashcard-back .flashcard-content p.whitespace-pre-wrap")
      .textContent()
  )?.trim() ?? "";

  expect(backLabel).toBe(isNameOnFront ? "Description" : "Name");
  expect(backContent).toBe(isNameOnFront ? vocab!.description : vocab!.name);
};

const checkVocabularyDisplay = async (
  page: Page,
  vocabulary: Vocabulary,
  row: number,
  offset: number = 0
) => {
  // row number
  await expect(
    page.locator("tr").nth(row).locator("td").nth(offset)
  ).toHaveText(row + "");
  // name
  await expect(
    page
      .locator("tr")
      .nth(row)
      .locator("td")
      .nth(offset + 1)
  ).toHaveText(vocabulary.name);
};
