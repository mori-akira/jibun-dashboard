const SHORT_MONTH_FORMATTER = new Intl.DateTimeFormat("en-US", {
  month: "short",
  timeZone: "UTC",
});

/**
 * 月(1〜12)を英語の短縮表記(Jan, Feb, ...)にする。
 * 画面表記を表示言語に依存させないため locale は固定。
 */
export function getShortMonthLabel(month: number): string {
  if (!Number.isInteger(month) || month < 1 || month > 12) {
    return "";
  }
  return SHORT_MONTH_FORMATTER.format(Date.UTC(2000, month - 1, 1));
}

export function formatToJST(value: string | undefined): string | undefined {
  if (!value) return undefined;
  return new Date(value).toLocaleString("sv-SE", {
    timeZone: "Asia/Tokyo",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
}

export function getTodayJST(): string {
  const localeString = new Date().toLocaleString("sv-SE", {
    timeZone: "Asia/Tokyo",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
  return localeString.split(" ")[0] ?? "";
}

export function getCurrentMonthFirstDateString(
  timeZone = "Asia/Tokyo",
): string {
  const now = new Date();
  const localeString = now.toLocaleString("sv-SE", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
  const [datePart] = localeString.split(" ");
  if (!datePart) {
    return "";
  }
  const [year, month] = datePart.split("-");
  return `${year}-${month}-01`;
}
