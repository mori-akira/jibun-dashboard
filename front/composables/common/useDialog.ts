import { ref } from "vue";

export const useInfoDialog = () => {
  const showInfoDialog = ref(false);
  const InfoDialogMessage = ref("");
  let resolver: (() => void) | null = null;

  const openInfoDialog = (message: string): Promise<void> => {
    showInfoDialog.value = true;
    InfoDialogMessage.value = message;

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const onInfoOk = () => {
    showInfoDialog.value = false;
    resolver?.();
    resolver = null;
  };

  return {
    showInfoDialog,
    InfoDialogMessage,
    openInfoDialog,
    onInfoOk,
  };
};

export const useConfirmDialog = () => {
  const showConfirmDialog = ref(false);
  const confirmDialogMessage = ref("");
  let resolver: ((result: boolean) => void) | null = null;

  const openConfirmDialog = (message: string): Promise<boolean> => {
    showConfirmDialog.value = true;
    confirmDialogMessage.value = message;

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const onConfirmYes = () => {
    showConfirmDialog.value = false;
    resolver?.(true);
    resolver = null;
  };

  const onConfirmNo = () => {
    showConfirmDialog.value = false;
    resolver?.(false);
    resolver = null;
  };

  return {
    showConfirmDialog,
    confirmDialogMessage,
    openConfirmDialog,
    onConfirmYes,
    onConfirmNo,
  };
};

export const useInputDialog = () => {
  const showInputDialog = ref(false);
  const inputDialogMessage = ref("");
  let resolver: ((result?: string) => void) | null = null;

  const openInputDialog = (message: string): Promise<string | undefined> => {
    showInputDialog.value = true;
    inputDialogMessage.value = message;

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const onInputOk = (inputValue?: string) => {
    showInputDialog.value = false;
    resolver?.(inputValue);
    resolver = null;
  };

  const onInputCancel = () => {
    showInputDialog.value = false;
    resolver?.(undefined);
    resolver = null;
  };

  return {
    showInputDialog,
    inputDialogMessage,
    openInputDialog,
    onInputOk,
    onInputCancel,
  };
};

export const useWarningDialog = () => {
  const showWarningDialog = ref(false);
  const warningDialogMessage = ref("");
  let resolver: (() => void) | null = null;

  const openWarningDialog = (message: string): Promise<void> => {
    showWarningDialog.value = true;
    warningDialogMessage.value = message;

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const onWarningOk = () => {
    showWarningDialog.value = false;
    resolver?.();
    resolver = null;
  };

  return {
    showWarningDialog,
    WarningDialogMessage: warningDialogMessage,
    openWarningDialog,
    onWarningOk,
  };
};
