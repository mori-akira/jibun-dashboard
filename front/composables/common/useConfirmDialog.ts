import { ref } from "vue";

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
