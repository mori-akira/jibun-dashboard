import { describe, it, expect } from "vitest";
import { z } from "zod";
import { zodToVeeRules } from "./zod-to-vee-rules";
import type { FieldValidationMetaInfo } from "./zod-to-vee-rules";

describe("zodToVeeRules", () => {
  it("minとmax長さチェック", () => {
    const schema = z.string().min(3).max(5);
    const rules = zodToVeeRules(schema);
    expect(rules[0]("ab", {} as FieldValidationMetaInfo)).toBe(
      "3文字以上で入力してください"
    );
    expect(rules[0]("abc", {} as FieldValidationMetaInfo)).toBe(true);
    expect(rules[1]("abcdef", {} as FieldValidationMetaInfo)).toBe(
      "5文字以下で入力してください"
    );
  });

  it("emailチェック", () => {
    const schema = z.string().email();
    const rules = zodToVeeRules(schema);
    expect(rules[0]("not-an-email", {} as FieldValidationMetaInfo)).toBe(
      "メールアドレスの形式で入力してください"
    );
    expect(rules[0]("test@example.com", {} as FieldValidationMetaInfo)).toBe(
      true
    );
  });

  it("正規表現チェック", () => {
    const schema = z.string().regex(/^[a-z]+$/);
    const rules = zodToVeeRules(schema);
    expect(rules[0]("ABC", {} as FieldValidationMetaInfo)).toBe(
      "使用できない文字が含まれています"
    );
    expect(rules[0]("abc", {} as FieldValidationMetaInfo)).toBe(true);
  });

  it("数値のmin/max/intチェック", () => {
    const schema = z.number().min(5).max(10).int();
    const rules = zodToVeeRules(schema);
    expect(rules[0]("4", {} as FieldValidationMetaInfo)).toBe(
      "5以上の数値を入力してください"
    );
    expect(rules[1]("11", {} as FieldValidationMetaInfo)).toBe(
      "10以下の数値を入力してください"
    );
    expect(rules[2]("5.5", {} as FieldValidationMetaInfo)).toBe(
      "整数を入力してください"
    );
  });
});
