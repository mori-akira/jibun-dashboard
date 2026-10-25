import { describe, it, expect } from "vitest";
import { buildJsonFileName } from "./download";

describe("buildJsonFileName", () => {
  it("日付の区切り文字を除いてファイル名を組み立てる", () => {
    expect(buildJsonFileName("salary", "2026-10-25")).toBe(
      "salary_20261025.json",
    );
  });

  it("日付が空の場合は接尾辞を付けない", () => {
    expect(buildJsonFileName("salary", "")).toBe("salary.json");
  });
});
