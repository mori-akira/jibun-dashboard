import { ZodNumber, ZodString, type ZodType } from "zod";
import { i18nT as t } from "~/utils/i18n";

// zod 4 のチェック定義（core の $ZodCheckDef 各種）のうち、ここで参照する項目のみ
type CheckDef = {
  check: string;
  value?: number;
  minimum?: number;
  maximum?: number;
  format?: string;
  pattern?: RegExp;
};

const getCheckDefs = (schema: ZodType): CheckDef[] =>
  (schema.def.checks ?? []).map((c) => c._zod.def as CheckDef);

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

const isEmptyInput = (v: unknown) =>
  v === undefined || v === null || (typeof v === "string" && v.trim() === "");

export type GenericValidateFunction<TValue = unknown> = (
  value: TValue,
  ctx: FieldValidationMetaInfo,
) => boolean | string;

export function zodToVeeRules(schema: ZodType): GenericValidateFunction[] {
  const rules: ((value: unknown, ctx?: unknown) => true | string)[] = [];

  if (!schema.isOptional()) {
    rules.push((value) =>
      isEmptyInput(value) ? t("message.validation.required") : true,
    );
  }

  if (schema instanceof ZodString) {
    for (const check of getCheckDefs(schema)) {
      if (check.check === "min_length") {
        const min = check.minimum!;
        rules.push((value) =>
          (value as string)?.length >= min
            ? true
            : t("message.validation.minCharacters", { min }),
        );
      } else if (check.check === "max_length") {
        const max = check.maximum!;
        rules.push((value) =>
          (value as string)?.length <= max
            ? true
            : t("message.validation.maxCharacters", { max }),
        );
      } else if (check.format === "regex") {
        const pattern = check.pattern!;
        rules.push((value) =>
          pattern.test(value as string)
            ? true
            : t("message.validation.invalidCharacters"),
        );
      } else if (check.format === "email") {
        rules.push((value) =>
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value as string)
            ? true
            : t("message.validation.email"),
        );
      }
    }
  }

  const zNumber =
    schema instanceof ZodNumber
      ? schema
      : (schema.def as { innerType?: unknown }).innerType;
  if (zNumber instanceof ZodNumber) {
    for (const check of getCheckDefs(zNumber)) {
      if (check.check === "greater_than") {
        const min = check.value!;
        rules.push((value) =>
          Number(value as string) >= min
            ? true
            : t("message.validation.minNumber", { min }),
        );
      } else if (check.check === "less_than") {
        const max = check.value!;
        rules.push((value) =>
          Number(value as string) <= max
            ? true
            : t("message.validation.maxNumber", { max }),
        );
      } else if (check.check === "number_format") {
        rules.push((value) =>
          Number.isInteger(Number(value))
            ? true
            : t("message.validation.integer"),
        );
      }
    }
  }

  return rules;
}
