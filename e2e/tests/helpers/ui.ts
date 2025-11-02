export async function openSubMenu(page: any, userName: string) {
  await page
    .getByRole("banner")
    .locator("div")
    .filter({ hasText: userName })
    .locator("div")
    .click();
}
