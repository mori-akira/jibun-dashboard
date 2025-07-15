export function getCurrentMonthFirstDateString(
  timeZone = "Asia/Tokyo"
): string {
  const now = new Date();
  const localeString = now.toLocaleString("sv-SE", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
  const [datePart] = localeString.split(" ");
  const [year, month] = datePart.split("-");
  return `${year}-${month}-01`;
}
