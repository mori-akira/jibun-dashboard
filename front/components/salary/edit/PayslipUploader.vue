<template>
  <div class="w-full flex">
    <FileUploader
      message=""
      wrapper-class="mt-4 h-48 flex-col"
      @upload="onUploadFile"
    >
      <div class="w-full flex justify-center font-cursive">
        Drag and drop a file here
      </div>
      <div class="w-full flex justify-center font-cursive">
        Or click to select one
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
  </div>
</template>

<script setup lang="ts">
import FileUploader from "~/components/common/FileUploader.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useConfirmDialog } from "~/composables/common/useConfirmDialog";
import { useCommonStore } from "~/stores/common";

const props = defineProps<{
  targetDate: string;
}>();

const commonStore = useCommonStore();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();

const onUploadFile = async (file: File) => {
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      "You Have Unsaved Change. Continue?"
    );
    if (!confirmed) {
      return;
    }
  }
  console.log(file.name);
  console.log(props.targetDate);
};
</script>
