import type { ZodTypeAny } from "zod";
import { ZodEffects, ZodNumber, ZodString } from "zod";
import { i18nT } from "~/utils/i18n";

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
            ? i18nT("message.validation.required")
            : i18nT("message.validation.minCharacters", { min: check.value })
        );
      } else if (check.kind === "max") {
        rules.push((value) =>
          (value as string)?.length <= check.value
            ? true
            : i18nT("message.validation.maxCharacters", { max: check.value })
        );
      } else if (check.kind === "regex") {
        rules.push((value) =>
          check.regex.test(value as string)
            ? true
            : i18nT("message.validation.invalidCharacters")
        );
      } else if (check.kind === "email") {
        rules.push((value) =>
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value as string)
            ? true
            : i18nT("message.validation.email")
        );
      }
    }
  }

  const zNumber = schema instanceof ZodNumber ? schema : schema._def?.innerType;
  if (zNumber instanceof ZodNumber) {
    const checks = zNumber._def.checks;
    for (const check of checks) {
      if (check.kind === "min") {
        rules.push((value) =>
          Number(value as string) >= check.value
            ? true
            : i18nT("message.validation.minNumber", { min: check.value })
        );
      } else if (check.kind === "max") {
        rules.push((value) =>
          Number(value as string) <= check.value
            ? true
            : i18nT("message.validation.maxNumber", { max: check.value })
        );
      } else if (check.kind === "int") {
        rules.push((value) =>
          Number.isInteger(Number(value))
            ? true
            : i18nT("message.validation.integer")
        );
      }
    }
  }

  return rules;
}
