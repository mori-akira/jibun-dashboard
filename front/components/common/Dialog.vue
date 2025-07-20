<template>
  <ModalWindow
    :show-modal="showDialog"
    modal-box-class="w-40vw"
    @close="onClose"
  >
    <div class="relative">
      <IconButton
        type="cancel"
        wrapper-class="absolute"
        icon-class="w-8 h-8 text-white translate-x-40vw -translate-y-14"
        @click="onClose"
      />
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
        <div class="flex flex-col items-center ml-4">
          <div>
            <span :class="[messageClass]">{{ message }}</span>
          </div>
          <div v-if="type === 'input'" class="mt-4">
            <TextBox
              :value="inputValue"
              :input-class="['ml-0', inputClass ?? '']"
              @change:value="onChangeInputValue"
            ></TextBox>
          </div>
        </div>
      </div>
      <div v-if="buttonType === 'ok'" class="mt-4 flex justify-center">
        <Button type="marked" @click="onClickOk">OK</Button>
      </div>
      <div v-if="buttonType === 'okCancel'" class="mt-4 flex justify-center">
        <Button type="default" @click="onClickCancel">Cancel</Button>
        <Button type="marked" wrapper-class="ml-8" @click="onClickOk"
          >OK</Button
        >
      </div>
      <div v-if="buttonType === 'yesNo'" class="mt-4 flex justify-center">
        <Button type="default" @click="onClickNo">No</Button>
        <Button type="marked" wrapper-class="ml-8" @click="onClickYes"
          >Yes</Button
        >
      </div>
    </div>
  </ModalWindow>
</template>

<script setup lang="ts">
import ModalWindow from "~/components/common/ModalWindow.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "./IconButton.vue";
import TextBox from "./TextBox.vue";

export type DialogType = "info" | "confirm" | "input" | "warning" | "error";

defineProps<{
  showDialog: boolean;
  type?: DialogType;
  iconName?: string;
  message?: string;
  buttonType?: "ok" | "okCancel" | "yesNo";
  messageClass?: string;
  inputClass?: string;
}>();

const emit = defineEmits<{
  (event: "close" | "click:cancel" | "click:yes" | "click:no"): void;
  (event: "click:ok", inputValue?: string): void;
}>();

const inputValue = ref<string>("");
const onChangeInputValue = (newValue: string) => (inputValue.value = newValue);

const onClose = (): void => {
  emit("close");
  inputValue.value = "";
};
const onClickOk = (): void => {
  emit("click:ok", inputValue.value);
  inputValue.value = "";
};
const onClickCancel = (): void => {
  emit("click:cancel");
  inputValue.value = "";
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
    case "input":
      return "tabler:pencil-down";
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
    case "input":
      return "text-pink-600";
    case "warning":
      return "text-yellow-600";
    case "error":
      return "text-red-700";
  }
};
</script>
