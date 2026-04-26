<template>
  <div :class="['px-12 py-3', wrapperClass]">
    <div v-if="topTags.length === 0" class="text-gray-400 text-xs">No data</div>
    <div class="grid grid-cols-2 gap-x-4 gap-y-2">
      <div
        v-for="item in topTags"
        :key="item.id"
        class="flex items-center gap-2"
      >
        <span
          class="shrink-0 py-[0.15rem] px-2 text-[0.9rem] text-white rounded-lg bg-[#888] max-w-[10rem] truncate"
        >
          {{ item.name }}
        </span>
        <span
          :class="[
            'ml-auto font-bold text-[#bb88ff]',
            { 'cursor-pointer underline': onClickTag },
          ]"
          @click="onClickTag?.(item.id)"
          >{{ item.count }}</span
        >
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Vocabulary } from "~/generated/api/client/api";
import { getTopTagCounts } from "~/utils/vocabulary";

const props = defineProps<{
  vocabularies: Vocabulary[];
  wrapperClass?: string;
  onClickTag?: (tagId: string) => void;
}>();

const topTags = computed(() => getTopTagCounts(props.vocabularies));
</script>
