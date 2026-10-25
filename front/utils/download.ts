/**
 * `<prefix>_<yyyyMMdd>.json` 形式のファイル名を組み立てる。
 * date は `yyyy-MM-dd` 形式を想定（区切り文字は除去する）。
 */
export function buildJsonFileName(prefix: string, date: string): string {
  const compactDate = date.replaceAll("-", "");
  return compactDate ? `${prefix}_${compactDate}.json` : `${prefix}.json`;
}

/**
 * 任意のオブジェクトを JSON ファイルとしてブラウザにダウンロードさせる。
 */
export function downloadJson(data: unknown, fileName: string): void {
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: "application/json",
  });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName;
  anchor.click();
  URL.revokeObjectURL(url);
}
