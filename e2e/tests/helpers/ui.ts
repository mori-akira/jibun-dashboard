export async function openSubMenu(page: any, userName: string) {
  await page
    .getByRole("banner")
    .locator("div")
    .filter({ hasText: userName })
    .locator("div")
    .click();
}

export async function openMobileMenu(page: any) {
  await page
    .getByRole('banner')
    .locator('div')
    .first()
    .click();
}
