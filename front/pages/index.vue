<template>
  <h2>
    <Icon name="tabler:home" class="adjust-icon" />
    <span class="font-cursive font-bold ml-2">Home</span>
    <div>
      <button type="button" @click="onAddError">エラー追加</button>
    </div>
    <div>
      <button type="button" @click="onClearError">エラークリア</button>
    </div>
  </h2>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
const commonStore = useCommonStore();

const onAddError = () => {
  commonStore.addErrorMessage("This is a test error message.");
};
const onClearError = () => {
  commonStore.clearErrorMessages();
};
</script>

<style scoped></style>
