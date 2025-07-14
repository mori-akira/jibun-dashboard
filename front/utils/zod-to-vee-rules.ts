import type { ZodTypeAny } from "zod";
import { ZodEffects, ZodNumber, ZodString } from "zod";

export type FieldValidationMetaInfo = {
  field: string;
  name: string;
  label?: string;
  value: unknown;
  form: Record<string, unknown>;
  rule?: {
    name: string;
    params?: Record<string, unknown> | unknown[];
  };
};

export type GenericValidateFunction<TValue = unknown> = (
  value: TValue,
  ctx: FieldValidationMetaInfo
) => boolean | string;

export function zodToVeeRules(schema: ZodTypeAny): GenericValidateFunction[] {
  const rules: ((value: unknown, ctx?: unknown) => true | string)[] = [];

  if (schema instanceof ZodEffects) {
    schema = schema._def.schema;
  }

  if (schema instanceof ZodString) {
    const checks = schema._def.checks;
    for (const check of checks) {
      if (check.kind === "min") {
        rules.push((value) =>
          (value as string)?.length >= check.value
            ? true
            : check.value === 1
            ? "入力必須項目です"
            : `${check.value}文字以上で入力してください`
        );
      } else if (check.kind === "max") {
        rules.push((value) =>
          (value as string)?.length <= check.value
            ? true
            : `${check.value}文字以下で入力してください`
        );
      } else if (check.kind === "regex") {
        rules.push((value) =>
          check.regex.test(value as string)
            ? true
            : `使用できない文字が含まれています`
        );
      } else if (check.kind === "email") {
        rules.push((value) =>
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value as string)
            ? true
            : "メールアドレスの形式で入力してください"
        );
      }
    }
  }

  if (
    schema instanceof ZodNumber ||
    schema._def.innerType instanceof ZodNumber
  ) {
    const checks =
      schema instanceof ZodNumber
        ? schema._def.checks
        : schema._def.innerType._def.checks;
    for (const check of checks) {
      if (check.kind === "min") {
        rules.push((value) =>
          Number(value as string) >= check.value
            ? true
            : `${check.value}以上の数値を入力してください`
        );
      } else if (check.kind === "max") {
        rules.push((value) =>
          Number(value as string) <= check.value
            ? true
            : `${check.value}以下の数値を入力してください`
        );
      } else if (check.kind === "int") {
        rules.push((value) =>
          Number.isInteger(Number(value)) ? true : `整数を入力してください`
        );
      }
    }
  }

  return rules;
}
