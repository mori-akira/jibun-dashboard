import type { ZodTypeAny } from "zod";
import { ZodEffects, ZodNumber, ZodString } from "zod";

export function zodToVeeRules(
  schema: ZodTypeAny
): ((value: unknown) => true | string)[] {
  const rules: ((value: unknown) => true | string)[] = [];

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
      } else if (check.kind === "email") {
        rules.push((value) =>
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value as string)
            ? true
            : "メールアドレスの形式で入力してください"
        );
      }
      // TODO: patternも実装
    }
  }

  if (schema instanceof ZodNumber) {
    const checks = schema._def.checks;
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
          Number.isInteger(value as number) ? true : `整数を入力してください`
        );
      }
    }
  }

  return rules;
}
