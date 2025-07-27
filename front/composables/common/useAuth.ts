import { jwtDecode } from "jwt-decode";

type JwtPayload = {
  exp: number;
  [key: string]: unknown;
};

export const useAuth = () => {
  const getIdToken = (): string | null => {
    if (!import.meta.client) {
      return null;
    }
    return localStorage.getItem("id_token");
  };
  const getAccessToken = (): string | null => {
    if (!import.meta.client) {
      return null;
    }
    return localStorage.getItem("access_token");
  };

  const isTokenExpired = (): boolean => {
    const token = getAccessToken();
    if (!token) {
      return true;
    }
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      const now = Math.floor(Date.now() / 1000);
      return decoded.exp < now;
    } catch {
      return true;
    }
  };

  return {
    getIdToken,
    getAccessToken,
    isTokenExpired,
  };
};
