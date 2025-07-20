import { ref } from "vue";

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
