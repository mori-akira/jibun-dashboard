import { execSync } from "node:child_process";
import { readFileSync, writeFileSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const projectRoot = resolve(__dirname, "..", "..");
const openapiDir = resolve(projectRoot, "openapi");
const e2eDir = resolve(projectRoot, "e2e");
const outputDir = resolve(e2eDir, "generated/api/client");

function run(cmd, options = {}) {
  console.log(`> ${cmd}`);
  execSync(cmd, { stdio: "inherit", ...options });
}

// OpenAPI GeneratorでTSクライアント生成
run(
  [
    "npx --yes @openapitools/openapi-generator-cli@2.21.0 generate",
    "-g typescript-axios",
    "-i openapi.yaml",
    `-o "${outputDir}"`,
    '--additional-properties supportsES6=true,withInterfaces=true',
    "--global-property apiDocs=false,modelDocs=false",
  ].join(" "),
  { cwd: openapiDir }
);

// LocaleCodeのaliasをapi.tsに追記
const apiTsPath = resolve(e2eDir, "generated/api/client/api.ts");
let apiTs = readFileSync(apiTsPath, "utf8");
const localeExport = "export type LocaleCode = I18n;";
if (!apiTs.includes(localeExport)) {
  apiTs += `\n${localeExport}\n`;
  writeFileSync(apiTsPath, apiTs);
  console.log("Appended LocaleCode alias to api.ts");
}

// openapi-zod-clientでzodスキーマ生成
run(
  "npx --yes openapi-zod-client openapi.yaml -o ../e2e/generated/api/client/schemas.ts --export-schemas",
  { cwd: openapiDir }
);

// schemas.tsの先頭にESLint無効コメントを付与
const schemasPath = resolve(e2eDir, "generated/api/client/schemas.ts");
let schemas = readFileSync(schemasPath, "utf8");
if (!schemas.startsWith("/* eslint-disable */")) {
  schemas = "/* eslint-disable */\n" + schemas;
  writeFileSync(schemasPath, schemas);
  console.log("Prepended /* eslint-disable */ to schemas.ts");
}

// prettierで整形
run('npx --yes prettier@3 --write "generated/api/client/**/*.{ts,tsx,js,json}"', {
  cwd: e2eDir,
});
