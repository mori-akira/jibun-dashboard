import fs from "fs";
import path from "path";

const STORAGE_STATE = path.resolve(
  __dirname,
  "../../.playwright/auth/user.json"
);

type StorageState = {
  cookies: unknown[];
  origins: {
    origin: string;
    localStorage: { name: string; value: string }[];
  }[];
};

export function getTokenFromStorageState(origin: string): string | null {
  if (origin.endsWith("/")) {
    origin = origin.slice(0, -1);
  }
  const raw = fs.readFileSync(STORAGE_STATE, "utf-8");
  const state = JSON.parse(raw) as StorageState;
  const originData = state.origins.find((o) => o.origin === origin);
  if (!originData) {
    return null;
  }

  const accessToken = originData.localStorage.find(
    (o) => o.name === "access_token"
  );
  if (accessToken) {
    return accessToken.value;
  }

  const idToken = originData.localStorage.find((o) => o.name === "id_token");
  return idToken ? idToken.value : null;
}
