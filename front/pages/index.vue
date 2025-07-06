<template>
  <h2>
    <Icon name="tabler:home" class="adjust-icon" />
    <span class="font-cursive font-bold ml-2">Home</span>
    <div>
      <Button wrapper-class="mt-4" @click="onAddError"> Add Error </Button>
    </div>
  </h2>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
import Button from "~/components/common/Button.vue";
const commonStore = useCommonStore();

const onAddError = () => {
  commonStore.addErrorMessage("This is a test error message.");
};
</script>
