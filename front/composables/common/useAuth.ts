import { jwtDecode } from "jwt-decode";

type JwtPayload = {
  exp: number;
  [key: string]: unknown;
};

export const useAuth = () => {
  const getIdToken = () => localStorage.getItem("id_token");
  const getAccessToken = () => localStorage.getItem("access_token");

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
