<template>
  <div class="flex justify-end">
    <IconButton type="cancel" icon-class="w-6 h-6" @click="onCloseModal" />
  </div>

  <ClientOnly>
    <Draggable
      v-model="localList"
      item-key="qualificationId"
      :animation="150"
      handle=".drag-handle"
      class="divide-y divide-gray-300 border border-gray-300 mt-4"
      @end="onDragEnd"
    >
      <template #item="{ element }">
        <div
          :key="element.qualificationId"
          class="flex items-center justify-between px-2 py-2"
        >
          <div class="flex items-center min-w-0">
            <Icon
              name="tabler:grip-vertical"
              class="drag-handle cursor-grab text-gray-600 shrink-0 translate-y-0.25"
            />
            <span class="w-8 text-center text-gray-600 shrink-0">
              {{ element.order }}
            </span>
            <span
              class="ml-2 w-full flex-1 truncate font-bold text-black"
              :title="element.qualificationName"
            >
              {{ element.qualificationName }}
            </span>
          </div>
        </div>
      </template>
    </Draggable>
  </ClientOnly>

  <div class="flex justify-center">
    <Button
      type="action"
      wrapper-class="flex justify-center mt-4"
      @click="onSubmit"
    >
      <Icon name="tabler:database-share" class="text-base translate-y-0.5" />
      <span class="ml-2">Execute</span>
    </Button>
  </div>
</template>

<script lang="ts" setup>
import Draggable from "vuedraggable";

import type { Qualification } from "~/generated/api/client";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import { useCommonStore } from "~/stores/common";

const props = defineProps<{
  targetQualifications: Qualification[] | undefined;
}>();
const emit = defineEmits<{
  (e: "closeModal"): void;
  (e: "submit", values: Qualification[]): void;
}>();

const commonStore = useCommonStore();
const localList = ref<Qualification[]>([]);

watch(
  () => props.targetQualifications,
  (newVal) => {
    localList.value = newVal ? newVal.map((e) => ({ ...e })) : [];
  },
  { immediate: true }
);

const onDragEnd = () => {
  if (localList.value.some((item, index) => item.order !== index + 1)) {
    localList.value = localList.value.map((item, index) => ({
      ...item,
      order: index + 1,
    }));
    commonStore.setHasUnsavedChange(true);
  }
};
const onSubmit = () => {
  emit("submit", localList.value ?? []);
};
const onCloseModal = () => {
  emit("closeModal");
};
</script>
