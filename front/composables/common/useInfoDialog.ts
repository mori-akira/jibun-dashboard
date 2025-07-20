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
