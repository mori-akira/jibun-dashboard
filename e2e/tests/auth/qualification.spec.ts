import { test, expect, Page } from "@playwright/test";
import testData from "./qualification.spec.data.json";

test("test qualification function", async ({ page }) => {
  // access to qualification
  await page.goto("/qualification", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check qualification list
  await expect(page.getByText("No Item")).toBeVisible();

  // === add qualifications ===
  // access to edit
  await page.getByRole("button", { name: "Edit" }).click();

  // add qualifications
  for (const qualification of testData) {
    await addQualification(page, qualification as Qualification);
    await page.waitForTimeout(100);
  }

  // check added qualifications
  for (const index in testData) {
    const qualification: Qualification = testData[index] as Qualification;
    await checkQualificationDisplay(page, qualification, Number(index) + 1, 1);
  }

  // add dummy qualification
  const dummyQualification: Qualification = {
    qualificationName: "Dummy Qualification",
    status: "planning",
    rank: "D",
    organization: "Dummy Org",
    officialUrl: "https://dummy.official.url",
  };
  await addQualification(page, dummyQualification);
  await checkQualificationDisplay(page, dummyQualification, 6, 1);

  // delete dummy qualification
  await page.locator("tr:nth-child(6) > td > .wrapper > .iconify").click();
  await page.getByRole("button", { name: "Delete All" }).click();
  await expect(
    page.getByText("Confirm deletion of all checked qualifications?")
  ).toBeVisible();
  await page.getByRole("button", { name: "Yes" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();
  await expect(page.getByText("Dummy Qualification")).toHaveCount(0);

  // === list qualifications ===
  // check qualification list
  await page
    .getByRole("main")
    .getByRole("link", { name: "Qualification" })
    .click();

  // check summary
  await checkSummaryDisplay(page, 1, 1, 2, 1);

  // check listed qualifications
  for (const index in testData) {
    const qualification: Qualification = testData[index] as Qualification;
    await checkQualificationDisplay(page, qualification, Number(index) + 1, 0);
  }

  // === home ===
  await page.getByTestId("app-header-title").click();

  // check summary
  await checkSummaryDisplay(page, 1, 1, 2, 1);

  // === mobile home ===
  await page.goto("/m", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check summary
  await checkSummaryDisplay(page, 1, 1, 2, 1);

  // === mobile qualification list ===
  await page.goto("/m/qualification", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // check summary
  await checkSummaryDisplay(page, 1, 1, 2, 1);

  // check listed qualifications
  for (const index in testData) {
    const qualification: Qualification = testData[index] as Qualification;
    await checkQualificationDisplay(page, qualification, Number(index) + 1, 0);
  }

  // === delete all qualifications ===
  // access to edit
  await page.goto("/qualification/edit", { waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");

  // delete all
  await page.locator(".iconify.i-tabler\\:square-dashed").first().click();
  await page.getByRole("button", { name: "Delete All" }).click();
  await expect(
    page.getByText("Confirm deletion of all checked qualifications?")
  ).toBeVisible();
  await page.getByRole("button", { name: "Yes" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();

  // check qualification list
  await expect(page.getByText("No Item")).toBeVisible();
});

type Qualification = {
  qualificationName: string;
  abbreviation?: string;
  version?: string;
  status: "acquired" | "planning" | "dream";
  rank: "D" | "C" | "B" | "A";
  organization: string;
  officialUrl: string;
  certificationUrl?: string;
  badgeUrl?: string;
};

const addQualification = async (page: Page, qualification: Qualification) => {
  // open modal
  await page.getByRole("button", { name: "Add New One" }).click();
  await page.waitForTimeout(100);
  // qualification name
  await page
    .locator("label:has-text('Qualification Name') + div > input")
    .last()
    .fill(qualification.qualificationName ?? "");
  // abbreviation
  await page
    .locator("label:has-text('Abbreviation') + div > input")
    .last()
    .fill(qualification.abbreviation ?? "");
  // version
  await page
    .locator("label:has-text('Version') + div > input")
    .last()
    .fill(qualification.version ?? "");
  // status
  await page.getByLabel("Status*").selectOption(qualification.status);
  // rank
  await page.getByLabel("Rank*").selectOption(qualification.rank);
  // organization
  await page
    .locator("label:has-text('Organization') + div > input")
    .last()
    .fill(qualification.organization ?? "");
  // official url
  await page
    .locator("label:has-text('Official Url') + div > input")
    .last()
    .fill(qualification.officialUrl ?? "");
  // certification url
  await page
    .locator("label:has-text('Certification Url') + div > input")
    .last()
    .fill(qualification.certificationUrl ?? "");
  // badge url
  await page
    .locator("label:has-text('Badge Url') + div > input")
    .last()
    .fill(qualification.badgeUrl ?? "");
  // execute
  await page.getByRole("button", { name: "Execute" }).click();
  await expect(page.getByText("Process completed successfully")).toBeVisible();
  await page.getByRole("button", { name: "OK" }).click();
};

const checkQualificationDisplay = async (
  page: Page,
  qualification: Qualification,
  row: number,
  offset: number
) => {
  // row number
  await expect(
    page.locator("tr").nth(row).locator("td").nth(offset)
  ).toHaveText(row + "");
  // qualification name
  await expect(
    page
      .locator("tr")
      .nth(row)
      .locator("td")
      .nth(offset + 1)
  ).toHaveText(qualification.qualificationName);
  // status
  await expect(
    page
      .locator("tr")
      .nth(row)
      .locator("td")
      .nth(offset + 2)
  ).toHaveText(qualification.status);
  // rank
  await expect(
    page
      .locator("tr")
      .nth(row)
      .locator("td")
      .nth(offset + 3)
  ).toHaveText(qualification.rank);
};

const checkSummaryDisplay = async (
  page: Page,
  a: number,
  b: number,
  c: number,
  d: number
) => {
  await expect(page.locator("span:has-text('A :') + span")).toHaveText(a + "");
  await expect(page.locator("span:has-text('B :') + span")).toHaveText(b + "");
  await expect(page.locator("span:has-text('C :') + span")).toHaveText(c + "");
  await expect(page.locator("span:has-text('D :') + span")).toHaveText(d + "");
};
