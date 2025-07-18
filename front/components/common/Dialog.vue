<template>
  <ModalWindow
    :show-modal="showDialog"
    modal-box-class="w-40vw"
    @close="onClose"
  >
    <div class="flex items-center justify-center">
      <div class="flex items-center justify-center w-16">
        <template v-if="type">
          <Icon
            :name="getIconNameByType(type)"
            :class="['text-4xl', getIconColorByType(type)]"
          />
        </template>
        <template v-else-if="iconName">
          <Icon :name="iconName" class="text-4xl" />
        </template>
      </div>
      <div class="ml-4">
        <span :class="[messageClass]">{{ message }}</span>
      </div>
    </div>
    <div v-if="buttonType === 'ok'" class="mt-4 flex justify-center">
      <Button type="marked" @click="onClickOk">OK</Button>
    </div>
    <div v-if="buttonType === 'yesNo'" class="mt-4 flex justify-center">
      <Button type="default" @click="onClickNo">No</Button>
      <Button type="marked" wrapper-class="ml-8" @click="onClickYes"
        >Yes</Button
      >
    </div>
  </ModalWindow>
</template>

<script setup lang="ts">
import ModalWindow from "~/components/common/ModalWindow.vue";
import Button from "~/components/common/Button.vue";

export type DialogType = "info" | "confirm" | "warning" | "error";

defineProps<{
  showDialog: boolean;
  type?: DialogType;
  iconName?: string;
  message?: string;
  buttonType?: "ok" | "yesNo";
  messageClass?: string;
}>();

const emit = defineEmits<{
  (event: "close" | "click:ok" | "click:yes" | "click:no"): void;
}>();

const onClose = (): void => {
  emit("close");
};
const onClickOk = (): void => {
  emit("click:ok");
};
const onClickYes = (): void => {
  emit("click:yes");
};
const onClickNo = (): void => {
  emit("click:no");
};

const getIconNameByType = (type: DialogType) => {
  switch (type) {
    case "info":
      return "tabler:info-circle";
    case "confirm":
      return "tabler:help-circle";
    case "warning":
      return "tabler:alert-triangle";
    case "error":
      return "tabler:alert-circle";
  }
};
const getIconColorByType = (type: DialogType) => {
  switch (type) {
    case "info":
      return "text-blue-700";
    case "confirm":
      return "text-green-700";
    case "warning":
      return "text-yellow-600";
    case "error":
      return "text-red-700";
  }
};
</script>
