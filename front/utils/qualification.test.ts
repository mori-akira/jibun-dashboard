import { describe, it, expect } from "vitest";
import { getRankColorHexCode } from "./qualification";
import type { Rank } from "./qualification";

describe("getRankColorHexCode", () => {
  const mockSetting = {
    rankAColor: "#ff0000",
    rankBColor: "#00ff00",
    rankCColor: "#0000ff",
    rankDColor: "#ffff00",
  };

  it("A~D", () => {
    expect(getRankColorHexCode("A", mockSetting)).toBe("#ff0000");
    expect(getRankColorHexCode("B", mockSetting)).toBe("#00ff00");
    expect(getRankColorHexCode("C", mockSetting)).toBe("#0000ff");
    expect(getRankColorHexCode("D", mockSetting)).toBe("#ffff00");
  });

  it("未定義", () => {
    expect(getRankColorHexCode("E" as Rank, mockSetting)).toBe("#888");
  });
});
