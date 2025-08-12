import { AxiosError } from "axios";
import { i18nT as t } from "~/utils/i18n";

export function getErrorMessage(error: AxiosError | unknown): string {
  if (error instanceof AxiosError) {
    if (error.response?.status === 400) {
      return t("message.error.invalidInput");
    } else if (error.response?.status === 401) {
      return t("message.error.authenticationFailed");
    } else if (error.response?.status === 403) {
      return t("message.error.permissionDenied");
    } else if (error.response?.status) {
      return t("message.error.unexpectedErrorWithStatusCode", {
        statusCode: error.response.status,
      });
    } else {
      return t("message.error.unexpected");
    }
  } else {
    return t("message.error.unexpected");
  }
}
