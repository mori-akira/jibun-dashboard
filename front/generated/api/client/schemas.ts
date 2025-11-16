/* eslint-disable */
import { makeApi, Zodios, type ZodiosOptions } from "@zodios/core";
import { z } from "zod";

const I18n = z
  .object({
    localeCode: z.string().regex(/^[a-z]{2}$/),
    messages: z.record(z.string()),
  })
  .partial()
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
        .passthrough(),
    ),
  })
  .partial()
  .passthrough();
const UserId = z.object({ userId: z.string() }).partial().passthrough();
const UserBase = z
  .object({
    userName: z.string().min(1).max(64),
    emailAddress: z.string().min(1).max(256).email(),
  })
  .passthrough();
const User = UserId.and(UserBase);
const Password = z
  .object({
    oldPassword: z
      .string()
      .min(8)
      .max(32)
      .regex(/^[a-zA-Z0-9!\"#\$%&'\(\)=~\|@{}\[\]\+\*,\.\/\\<>?_]+$/),
    newPassword: z
      .string()
      .min(8)
      .max(32)
      .regex(/^[a-zA-Z0-9!\"#\$%&'\(\)=~\|@{}\[\]\+\*,\.\/\\<>?_]+$/),
  })
  .passthrough();
const Setting = z
  .object({
    userId: z.string().optional(),
    salary: z
      .object({
        financialYearStartMonth: z.number().int().gte(1).lte(12),
        transitionItemCount: z.number().int().gte(1).lte(10),
        compareDataColors: z
          .array(
            z.string().regex(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})/),
          )
          .min(3)
          .max(3),
      })
      .passthrough(),
    qualification: z
      .object({
        rankAColor: z
          .string()
          .regex(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})/),
        rankBColor: z
          .string()
          .regex(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})/),
        rankCColor: z
          .string()
          .regex(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})/),
        rankDColor: z
          .string()
          .regex(/#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})/),
      })
      .passthrough(),
  })
  .passthrough();
const UploadUrl = z
  .object({
    fileId: z.string().uuid(),
    uploadUrl: z.string().url(),
    expireDateTime: z.string().datetime({ offset: true }).optional(),
  })
  .passthrough();
const SalaryId = z
  .object({ salaryId: z.string().uuid() })
  .partial()
  .passthrough();
const SalaryBase = z
  .object({
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
            z.object({ key: z.string(), data: z.number() }).passthrough(),
          ),
        })
        .passthrough(),
    ),
  })
  .passthrough();
const Salary = SalaryId.and(SalaryBase);
const SalaryOcrTaskId = z
  .object({ ocrTaskId: z.string().uuid() })
  .partial()
  .passthrough();
const SalaryOcrTaskBase = z
  .object({
    status: z.enum(["PENDING", "RUNNING", "COMPLETED", "FAILED"]),
    targetDate: z.string(),
    createdAt: z.string().datetime({ offset: true }),
    updatedAt: z.string().datetime({ offset: true }),
  })
  .passthrough();
const SalaryOcrTask = SalaryOcrTaskId.and(SalaryOcrTaskBase);
const postSalaryOcrTaskStart_Body = z
  .object({ targetDate: z.string(), fileId: z.string().uuid() })
  .passthrough();
const QualificationId = z
  .object({ qualificationId: z.string().uuid() })
  .partial()
  .passthrough();
const QualificationBase = z
  .object({
    order: z.number().int().gte(1),
    qualificationName: z.string().min(1).max(128),
    abbreviation: z.string().max(128).optional(),
    version: z.string().max(128).optional(),
    status: z.enum(["dream", "planning", "acquired"]),
    rank: z.enum(["D", "C", "B", "A"]),
    organization: z.string().min(1).max(128),
    acquiredDate: z.string().optional(),
    expirationDate: z.string().optional(),
    officialUrl: z.string().url(),
    certificationUrl: z.string().url().optional(),
    badgeUrl: z.string().url().optional(),
  })
  .passthrough();
const Qualification = QualificationId.and(QualificationBase);
const VocabularyId = z
  .object({ vocabularyId: z.string().uuid() })
  .partial()
  .passthrough();
const VocabularyBase = z
  .object({
    name: z.string().max(128),
    description: z.string().max(2048).optional(),
    tags: z
      .array(
        z
          .object({
            tagId: z.string().uuid().optional(),
            tag: z.string().min(1).max(64),
          })
          .passthrough(),
      )
      .max(64)
      .optional(),
    createdDateTime: z.string().datetime({ offset: true }).optional(),
    updatedDateTime: z.string().datetime({ offset: true }).optional(),
  })
  .passthrough();
const Vocabulary = VocabularyId.and(VocabularyBase);

export const schemas = {
  I18n,
  ErrorInfo,
  UserId,
  UserBase,
  User,
  Password,
  Setting,
  UploadUrl,
  SalaryId,
  SalaryBase,
  Salary,
  SalaryOcrTaskId,
  SalaryOcrTaskBase,
  SalaryOcrTask,
  postSalaryOcrTaskStart_Body,
  QualificationId,
  QualificationBase,
  Qualification,
  VocabularyId,
  VocabularyBase,
  Vocabulary,
};

const endpoints = makeApi([
  {
    method: "put",
    path: "/file/upload-mock",
    alias: "uploadFileMock",
    requestFormat: "json",
    response: z.void(),
  },
  {
    method: "get",
    path: "/file/upload-url",
    alias: "getUploadUrl",
    description: `ファイルアップロード用の署名付きURLを発行し、取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "fileId",
        type: "Query",
        schema: z.string().uuid().optional(),
      },
      {
        name: "expiresIn",
        type: "Query",
        schema: z.number().int().gte(1).lte(604800).optional(),
      },
    ],
    response: UploadUrl,
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
    path: "/qualification",
    alias: "postQualification",
    description: `資格情報を登録する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: QualificationBase,
      },
    ],
    response: z
      .object({ qualificationId: z.string().uuid() })
      .partial()
      .passthrough(),
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
    path: "/qualification/:qualificationId",
    alias: "getQualificationById",
    description: `IDを指定して資格情報を取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "qualificationId",
        type: "Path",
        schema: z.string().uuid(),
      },
    ],
    response: Qualification,
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
    path: "/qualification/:qualificationId",
    alias: "putQualification",
    description: `資格情報を更新する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: QualificationBase,
      },
      {
        name: "qualificationId",
        type: "Path",
        schema: z.string().uuid(),
      },
    ],
    response: z
      .object({ qualificationId: z.string().uuid() })
      .partial()
      .passthrough(),
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
    path: "/qualification/:qualificationId",
    alias: "deleteQualification",
    description: `IDを指定して資格情報を削除する`,
    requestFormat: "json",
    parameters: [
      {
        name: "qualificationId",
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
    path: "/qualifications",
    alias: "getQualification",
    description: `検索条件を指定して資格情報を取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "qualificationName",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "status",
        type: "Query",
        schema: z.array(z.enum(["dream", "planning", "acquired"])).optional(),
      },
      {
        name: "rank",
        type: "Query",
        schema: z.array(z.enum(["D", "C", "B", "A"])).optional(),
      },
      {
        name: "organization",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "acquiredDateFrom",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "acquiredDateTo",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "expirationDateFrom",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "expirationDateTo",
        type: "Query",
        schema: z.string().optional(),
      },
    ],
    response: z.array(Qualification),
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
    path: "/resources/i18n/:localeCode",
    alias: "getI18n",
    description: `国際化リソースを取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "localeCode",
        type: "Path",
        schema: z.string().regex(/^[a-z]{2}$/),
      },
    ],
    response: I18n,
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
    path: "/salaries",
    alias: "getSalary",
    description: `検索条件を指定して給与情報を取得する`,
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
    method: "post",
    path: "/salary",
    alias: "postSalary",
    description: `給与情報を登録する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: SalaryBase,
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
    method: "put",
    path: "/salary/:salaryId",
    alias: "putSalary",
    description: `給与情報を更新する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: SalaryBase,
      },
      {
        name: "salaryId",
        type: "Path",
        schema: z.string().uuid(),
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
    path: "/salary/ocr-task",
    alias: "getSalaryOcrTask",
    description: `給与OCRタスクを取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "targetDate",
        type: "Query",
        schema: z.string(),
      },
    ],
    response: z.array(SalaryOcrTask),
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
    path: "/salary/ocr-task/:ocrTaskId",
    alias: "getSalaryOcrTaskById",
    description: `IDを指定して給与OCRタスクを取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "ocrTaskId",
        type: "Path",
        schema: z.string().uuid(),
      },
    ],
    response: SalaryOcrTask,
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
    path: "/salary/ocr-task/start",
    alias: "postSalaryOcrTaskStart",
    description: `給与OCRタスクを開始する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: postSalaryOcrTaskStart_Body,
      },
    ],
    response: z
      .object({ ocrTaskId: z.string().uuid() })
      .partial()
      .passthrough(),
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
    path: "/setting",
    alias: "getSetting",
    description: `アクセストークンを用いて、現在ログイン中のユーザの設定を取得する`,
    requestFormat: "json",
    response: Setting,
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
    path: "/setting",
    alias: "putSetting",
    description: `アクセストークンを用いて、現在ログイン中のユーザの設定を登録(登録済みの場合は情報を置き換え)する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: Setting,
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
    description: `アクセストークンを用いて、現在ログイン中のユーザ情報を更新する`,
    requestFormat: "json",
    parameters: [
      {
        name: "body",
        type: "Body",
        schema: UserBase,
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
  {
    method: "get",
    path: "/vocabulary",
    alias: "getVocabulary",
    description: `検索条件を指定してボキャブラリーを取得する`,
    requestFormat: "json",
    parameters: [
      {
        name: "vocabularyName",
        type: "Query",
        schema: z.string().optional(),
      },
      {
        name: "tags",
        type: "Query",
        schema: z.array(z.string()).optional(),
      },
    ],
    response: z.array(Vocabulary),
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
