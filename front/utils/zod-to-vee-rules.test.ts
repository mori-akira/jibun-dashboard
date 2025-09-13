// zod-to-vee-rules.test.ts
import { describe, it, expect, vi, afterEach } from "vitest";
import { z } from "zod";
import { zodToVeeRules } from "./zod-to-vee-rules";
import type { FieldValidationMetaInfo } from "./zod-to-vee-rules";

vi.mock("~/utils/i18n", () => {
  return {
    i18nT: vi.fn((key: string, named?: Record<string, unknown>) => ({
      key,
      named,
    })),
  };
});

afterEach(() => {
  vi.clearAllMocks();
});

describe("zodToVeeRules", () => {
  it("必須チェック", () => {
    const schema = z.string();
    const rules = zodToVeeRules(schema);

    expect(rules[0]?.("", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.required",
      named: undefined,
    });
  });

  it("min 文字数チェック（3文字以上）", () => {
    const schema = z.string().min(3);
    const rules = zodToVeeRules(schema);

    expect(rules[1]?.("ab", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.minCharacters",
      named: { min: 3 },
    });
    expect(rules[1]?.("abc", {} as FieldValidationMetaInfo)).toBe(true);
  });

  it("max 文字数チェック（5文字以下）", () => {
    const schema = z.string().max(5);
    const rules = zodToVeeRules(schema);

    expect(rules[1]?.("abcdef", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.maxCharacters",
      named: { max: 5 },
    });
    expect(rules[1]?.("abc", {} as FieldValidationMetaInfo)).toBe(true);
  });

  it("email チェック", () => {
    const schema = z.string().email();
    const rules = zodToVeeRules(schema);

    expect(rules[1]?.("not-an-email", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.email",
      named: undefined,
    });
    expect(rules[1]?.("test@example.com", {} as FieldValidationMetaInfo)).toBe(
      true
    );
  });

  it("正規表現チェック", () => {
    const schema = z.string().regex(/^[a-z]+$/);
    const rules = zodToVeeRules(schema);

    expect(rules[1]?.("ABC", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.invalidCharacters",
      named: undefined,
    });
    expect(rules[1]?.("abc", {} as FieldValidationMetaInfo)).toBe(true);
  });

  it("数値の min/max/int チェック", () => {
    const schema = z.number().min(5).max(10).int();
    const rules = zodToVeeRules(schema);

    expect(rules[1]?.("4", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.minNumber",
      named: { min: 5 },
    });
    expect(rules[2]?.("11", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.maxNumber",
      named: { max: 10 },
    });
    expect(rules[3]?.("5.5", {} as FieldValidationMetaInfo)).toEqual({
      key: "message.validation.integer",
      named: undefined,
    });
  });
});
