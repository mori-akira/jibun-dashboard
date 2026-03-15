<template>
  <div class="w-full flex">
    <FileUploader
      message=""
      wrapper-class="mt-4 h-48 flex-col"
      @upload="onUploadFile"
    >
      <div class="w-full flex justify-center">
        <Icon name="tabler:drag-drop" class="adjust-icon-2" />
        <span class="ml-2">Drag And Drop File Here</span>
      </div>
      <div class="w-full flex justify-center mt-2">
        <Icon name="tabler:hand-click" class="adjust-icon-2" />
        <span class="ml-2">Or Click Here To Select One</span>
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
      :message="warningDialogMessage"
      button-type="ok"
      @click:ok="onWarningOk"
      @close="onWarningOk"
    />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";

import FileUploader from "~/components/common/FileUploader.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useConfirmDialog } from "~/composables/common/useDialog";
import { useFileCheck } from "~/composables/common/useFileCheck";
import { useCommonStore } from "~/stores/common";

const emits = defineEmits<{
  (event: "upload", file: File): void;
}>();

const { t } = useI18n();
const commonStore = useCommonStore();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();
const { checkFile, showWarningDialog, warningDialogMessage, onWarningOk } =
  useFileCheck({ maxSizeMB: 1 });

const onUploadFile = async (file: File) => {
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      t("message.confirm.checkUnsavedChanges"),
    );
    if (!confirmed) {
      return;
    }
  }
  if (!(await checkFile(file))) {
    return;
  }
  emits("upload", file);
};
</script>
