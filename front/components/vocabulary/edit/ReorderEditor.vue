<template>
  <div class="flex justify-end">
    <IconButton
      type="cancel"
      icon-class="w-6 h-6"
      @click:button="onCloseModal"
    />
  </div>

  <ClientOnly>
    <Draggable
      v-model="localList"
      item-key="vocabularyTagId"
      :animation="150"
      handle=".drag-handle"
      class="divide-y divide-gray-300 border border-gray-300 mt-4"
      @end="onDragEnd"
    >
      <template #item="{ element }">
        <div
          :key="element.vocabularyTagId"
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
              class="ml-2 flex-1 truncate font-bold text-black"
              :title="element.vocabularyTag"
            >
              {{ element.vocabularyTag }}
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
      @click:button="onSubmit"
    >
      <Icon name="tabler:database-share" class="text-base translate-y-0.5" />
      <span class="ml-2">Execute</span>
    </Button>
  </div>
</template>

<script lang="ts" setup>
import Draggable from "vuedraggable";

import type { VocabularyTag } from "~/generated/api/client";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import { useCommonStore } from "~/stores/common";

const props = defineProps<{
  targetTags: VocabularyTag[] | undefined;
}>();

const emit = defineEmits<{
  (e: "closeModal"): void;
  (e: "submit", values: VocabularyTag[]): void;
}>();

const commonStore = useCommonStore();
const localList = ref<VocabularyTag[]>([]);

watch(
  () => props.targetTags,
  (newVal) => {
    localList.value = newVal ? newVal.map((e) => ({ ...e })) : [];
  },
  { immediate: true },
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

const onSubmit = () => emit("submit", localList.value);
const onCloseModal = () => emit("closeModal");
</script>
