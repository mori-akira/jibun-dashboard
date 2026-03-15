import { useI18n } from "vue-i18n";
import { useWarningDialog } from "~/composables/common/useDialog";

export const useFileCheck = (options?: {
  maxSizeMB?: number;
  extensions?: string[];
  mimeTypes?: string[];
}) => {
  const maxSizeMB = options?.maxSizeMB ?? 5;
  const extensions = options?.extensions ?? [".pdf"];
  const mimeTypes = options?.mimeTypes ?? ["application/pdf"];
  const { t } = useI18n();
  const {
    showWarningDialog,
    warningDialogMessage,
    openWarningDialog,
    onWarningOk,
  } = useWarningDialog();

  const checkFileExtension = (file: File) =>
    extensions.some((ext) => file.name.toLowerCase().endsWith(ext));
  const checkFileType = (file: File) => mimeTypes.includes(file.type);
  const checkFileSize = (file: File) => file.size <= maxSizeMB * 1024 * 1024;

  const checkFile = async (file: File): Promise<boolean> => {
    if (!checkFileExtension(file) || !checkFileType(file)) {
      await openWarningDialog(t("message.warning.onlyPDFAccepted"));
      return false;
    }
    if (!checkFileSize(file)) {
      await openWarningDialog(t("message.warning.fileSizeLimitExceeded"));
      return false;
    }
    return true;
  };

  return {
    checkFile,
    showWarningDialog,
    warningDialogMessage,
    onWarningOk,
  };
};
