import { makeApi, Zodios, type ZodiosOptions } from "@zodios/core";
import { z } from "zod";

const User = z
  .object({
    userId: z.string().optional(),
    userName: z.string().min(1).max(64),
    emailAddress: z.string().min(1).max(256).email(),
  })
  .passthrough();
const ErrorInfo = z
  .object({
    errors: z.array(
      z
        .object({
          errorCode: z.string(),
          errorLevel: z.enum(["DEBUG", "INFO", "WARN", "ERROR", "CRITICAL"]),
          errorMessage: z.string(),
          errorItem: z.array(z.string()).optional(),
        })
        .passthrough()
    ),
  })
  .partial()
  .passthrough();
const Password = z
  .object({
    oldPassword: z.string().min(8).max(32),
    newPassword: z.string().min(8).max(32),
  })
  .passthrough();
const Salary = z
  .object({
    salaryId: z.string().uuid().optional(),
    userId: z.string(),
    targetDate: z.string(),
    overview: z
      .object({
        grossIncome: z.number().int().gte(0),
        netIncome: z.number().int().gte(0),
        operatingTime: z.number().gte(0),
        overtime: z.number().gte(0),
        bonus: z.number().int().gte(0),
        bonusTakeHome: z.number().int().gte(0),
      })
      .passthrough(),
    structure: z
      .object({
        basicSalary: z.number().int().gte(0),
        overtimePay: z.number().int().gte(0),
        housingAllowance: z.number().int().gte(0),
        positionAllowance: z.number().int().gte(0),
        other: z.number().int().gte(0),
      })
      .passthrough(),
    payslipData: z.array(
      z
        .object({
          key: z.string(),
          data: z.array(
            z.object({ key: z.string(), data: z.number() }).passthrough()
          ),
        })
        .passthrough()
    ),
  })
  .passthrough();
const SalaryId = z
  .object({ salaryId: z.string().uuid() })
  .partial()
  .passthrough();

export const schemas = {
  User,
  ErrorInfo,
  Password,
  Salary,
  SalaryId,
};

const endpoints = makeApi([
  {
    method: "get",
    path: "/salary",
    alias: "getSalary",
    description: `単一の対象日付、または対象日付From~対象日付Toを指定して給与情報を取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "targetDate",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "targetDateFrom",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "targetDateTo",
        type: "Query",
        schema: z.string().optional(),
      },
    ],
    response: z.array(Salary),
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
  {
    method: "put",
    path: "/salary",
    alias: "putSalary",
    description: `給与情報を登録(登録済みの場合は情報を置き換え)する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: Salary,
      },
    ],
    response: z.object({ salaryId: z.string().uuid() }).partial().passthrough(),
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
  {
    method: "get",
    path: "/salary/:salaryId",
    alias: "getSalaryById",
    description: `IDを指定して給与情報を取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "salaryId",
        type: "Path",
        schema: z.string().uuid(),
      },
    ],
    response: Salary,
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
  {
    method: "delete",
    path: "/salary/:salaryId",
    alias: "deleteSalary",
    description: `IDを指定して給与情報を削除する`,
    requestFormat: "json",
    parameters: [
      {
        name: "salaryId",
        type: "Path",
        schema: z.string().uuid(),
      },
    ],
    response: z.void(),
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
  {
    method: "get",
    path: "/user",
    alias: "getUser",
    description: `アクセストークンを用いて、現在ログイン中のユーザ情報を取得する`,
    requestFormat: "json",
    response: User,
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
  {
    method: "put",
    path: "/user",
    alias: "putUser",
    description: `アクセストークンを用いて、現在ログイン中のユーザ情報を登録(登録済みの場合は情報を置き換え)する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: User,
      },
    ],
    response: z.void(),
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
  {
    method: "post",
    path: "/user/password",
    alias: "postPassword",
    description: `パスワード情報を更新する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: Password,
      },
    ],
    response: z.void(),
    errors: [
      {
        status: 400,
        description: `パラメータ不正`,
        schema: ErrorInfo,
      },
    ],
  },
]);

export const api = new Zodios(endpoints);

export function createApiClient(baseUrl: string, options?: ZodiosOptions) {
  return new Zodios(baseUrl, endpoints, options);
}
