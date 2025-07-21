<template>
  <div class="w-full flex">
    <FileUploader
      message=""
      wrapper-class="mt-4 h-48 flex-col"
      @upload="onUploadFile"
    >
      <div class="w-full flex justify-center font-cursive">
        <Icon name="tabler:drag-drop" class="adjust-icon-2" />
        <span class="ml-2">Drag And Drop File Here</span>
      </div>
      <div class="w-full flex justify-center font-cursive mt-2">
        <Icon name="tabler:hand-click" class="adjust-icon-2" />
        <span class="ml-2">Or Click To Select One</span>
      </div>
    </FileUploader>

    <Dialog
      :show-dialog="showConfirmDialog"
      type="confirm"
      :message="confirmDialogMessage"
      button-type="yesNo"
      @click:yes="onConfirmYes"
      @click:no="onConfirmNo"
      @close="onConfirmNo"
    />

    <Dialog
      :show-dialog="showWarningDialog"
      type="warning"
      :message="WarningDialogMessage"
      button-type="ok"
      @click:ok="onWarningOk"
      @close="onWarningOk"
    />
  </div>
</template>

<script setup lang="ts">
import FileUploader from "~/components/common/FileUploader.vue";
import Dialog from "~/components/common/Dialog.vue";
import {
  useConfirmDialog,
  useWarningDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";

const emits = defineEmits<{
  (event: "upload", file: File): void;
}>();

const commonStore = useCommonStore();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();
const {
  showWarningDialog,
  WarningDialogMessage,
  openWarningDialog,
  onWarningOk,
} = useWarningDialog();

const onUploadFile = async (file: File) => {
  const id = commonStore.addLoadingQueue();
  try {
    if (commonStore.hasUnsavedChange) {
      const confirmed = await openConfirmDialog(
        "You Have Unsaved Change. Continue?"
      );
      if (!confirmed) {
        return;
      }
    }
    if (!(await checkFile(file))) {
      return;
    }
    emits("upload", file);
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
};
const checkFile = async (file: File): Promise<boolean> => {
  if (!checkFileExtension(file) || !checkFileType(file)) {
    await openWarningDialog("Only PDF files are accepted.");
    return false;
  }
  if (!checkFileSize(file)) {
    await openConfirmDialog("The file size must be 1MB or less.");
    return false;
  }
  return true;
};
const checkFileExtension = (file: File) => {
  const fileName = file.name.toLowerCase();
  return fileName.endsWith(".pdf");
};
const checkFileType = (file: File) => {
  return file.type === "application/pdf";
};
const checkFileSize = (file: File) => {
  const maxSizeInBytes = 1 * 1024 * 1024;
  return file.size <= maxSizeInBytes;
};
</script>
