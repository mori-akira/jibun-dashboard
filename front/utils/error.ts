import { AxiosError } from "axios";

export function getErrorMessage(error: AxiosError | unknown): string {
  if (error instanceof AxiosError) {
    if (error.response?.status === 400) {
      return "入力内容に誤りがあります";
    } else if (error.response?.status === 401) {
      return "認証に失敗しました";
    } else if (error.response?.status === 403) {
      return "アクセス権限がありません";
    } else if (error.response?.status) {
      return `想定外のエラーが発生しました (ステータスコード: ${error.response?.status})`;
    } else {
      return "想定外のエラーが発生しました";
    }
  } else {
    return "想定外のエラーが発生しました";
  }
}
