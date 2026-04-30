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
