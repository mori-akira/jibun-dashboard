import { describe, it, expect } from "vitest";
import { matchCharacterTypeRule } from "./password";

describe("matchCharacterTypeRule", () => {
  it("3種", () => {
    expect(matchCharacterTypeRule("Aa1")).toBe(true);
    expect(matchCharacterTypeRule("A!a")).toBe(true);
  });

  it("2種", () => {
    expect(matchCharacterTypeRule("AaA")).toBe(false);
    expect(matchCharacterTypeRule("a1a")).toBe(false);
    expect(matchCharacterTypeRule("!1!")).toBe(false);
    expect(matchCharacterTypeRule("A?A")).toBe(false);
  });
});
