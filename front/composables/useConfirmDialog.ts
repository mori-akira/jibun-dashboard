import { ref } from "vue";

export const useConfirmDialog = () => {
  const showConfirmDialog = ref(false);
  const dialogMessage = ref("");
  let resolver: ((result: boolean) => void) | null = null;

  const openConfirmDialog = (message: string): Promise<boolean> => {
    showConfirmDialog.value = true;
    dialogMessage.value = message;

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const onYes = () => {
    showConfirmDialog.value = false;
    resolver?.(true);
    resolver = null;
  };

  const onNo = () => {
    showConfirmDialog.value = false;
    resolver?.(false);
    resolver = null;
  };

  return {
    showConfirmDialog,
    dialogMessage,
    openConfirmDialog,
    onYes,
    onNo,
  };
};
