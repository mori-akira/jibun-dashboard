<template>
  <div class="flex flex-col gap-2 mt-2">
    <TextBox
      label="Name"
      :model-value="vocabularyName"
      wrapper-class="items-center"
      label-class="w-24 ml-4 font-cursive"
      input-wrapper-class="w-[28rem]"
      @update:model-value="(v) => emit('update:vocabularyName', v)"
    />
    <TextBox
      label="Description"
      :model-value="description"
      wrapper-class="items-center"
      label-class="w-24 ml-4 font-cursive"
      input-wrapper-class="w-[28rem]"
      @update:model-value="(v) => emit('update:description', v)"
    />
    <div
      v-if="vocabularyTags.length > 0"
      class="flex flex-col sm:flex-row sm:items-center mt-2"
    >
      <span class="w-24 ml-4 font-cursive shrink-0">Tags</span>
      <MultiOptionSelector
        label=""
        :options="vocabularyTags.map((t) => t.vocabularyTag)"
        :values="vocabularyTags.map((t) => t.vocabularyTagId ?? '')"
        :selected-options="selectedTagIds"
        wrapper-class="flex flex-wrap gap-1 mt-2 sm:mt-0"
        @click:value="onClickTag"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { VocabularyTag } from "~/generated/api/client/api";
import TextBox from "~/components/common/TextBox.vue";
import MultiOptionSelector from "~/components/common/MultiOptionSelector.vue";

const props = defineProps<{
  vocabularyName: string;
  description: string;
  selectedTagIds: string[];
  vocabularyTags: VocabularyTag[];
}>();

const emit = defineEmits<{
  (e: "update:vocabularyName" | "update:description", value: string): void;
  (e: "update:selectedTagIds", value: string[]): void;
}>();

const onClickTag = (tagId: string) => {
  const current = [...props.selectedTagIds];
  const idx = current.indexOf(tagId);
  if (idx >= 0) {
    current.splice(idx, 1);
  } else {
    current.push(tagId);
  }
  emit("update:selectedTagIds", current);
};
</script>
