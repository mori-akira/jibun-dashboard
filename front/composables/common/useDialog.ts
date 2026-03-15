import { ref } from "vue";

const createDialogBase = <T>() => {
  const show = ref(false);
  const message = ref("");
  let resolver: ((result: T) => void) | null = null;

  const open = (msg: string): Promise<T> =>
    new Promise((resolve) => {
      show.value = true;
      message.value = msg;
      resolver = resolve;
    });

  const close = (result?: T) => {
    show.value = false;
    resolver?.(result as T);
    resolver = null;
  };

  return { show, message, open, close };
};

export const useInfoDialog = () => {
  const {
    show: showInfoDialog,
    message: infoDialogMessage,
    open: openInfoDialog,
    close,
  } = createDialogBase<undefined>();
  const onInfoOk = () => close();
  return { showInfoDialog, infoDialogMessage, openInfoDialog, onInfoOk };
};

export const useConfirmDialog = () => {
  const {
    show: showConfirmDialog,
    message: confirmDialogMessage,
    open: openConfirmDialog,
    close,
  } = createDialogBase<boolean>();
  const onConfirmYes = () => close(true);
  const onConfirmNo = () => close(false);
  return {
    showConfirmDialog,
    confirmDialogMessage,
    openConfirmDialog,
    onConfirmYes,
    onConfirmNo,
  };
};

export const useInputDialog = () => {
  const {
    show: showInputDialog,
    message: inputDialogMessage,
    open: openInputDialog,
    close,
  } = createDialogBase<string | undefined>();
  const onInputOk = (inputValue?: string) => close(inputValue);
  const onInputCancel = () => close(undefined);
  return {
    showInputDialog,
    inputDialogMessage,
    openInputDialog,
    onInputOk,
    onInputCancel,
  };
};

export const useWarningDialog = () => {
  const {
    show: showWarningDialog,
    message: warningDialogMessage,
    open: openWarningDialog,
    close,
  } = createDialogBase<undefined>();
  const onWarningOk = () => close();
  return {
    showWarningDialog,
    warningDialogMessage,
    openWarningDialog,
    onWarningOk,
  };
};

export const useErrorDialog = () => {
  const {
    show: showErrorDialog,
    message: errorDialogMessage,
    open: openErrorDialog,
    close,
  } = createDialogBase<undefined>();
  const onErrorOk = () => close();
  return { showErrorDialog, errorDialogMessage, openErrorDialog, onErrorOk };
};
