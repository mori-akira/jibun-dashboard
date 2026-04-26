<template>
  <div :class="['flex flex-col justify-center gap-2 px-12 py-3', wrapperClass]">
    <div v-if="topTags.length === 0" class="text-gray-400 text-xs">No data</div>
    <div v-for="item in topTags" :key="item.id" class="flex items-center gap-2">
      <span
        class="shrink-0 py-[0.15rem] px-2 text-[0.9rem] text-white rounded-lg bg-[#bb88ff] max-w-[10rem] truncate"
      >
        {{ item.name }}
      </span>
      <span class="ml-auto text-[#bb88ff]">:</span>
      <span
        :class="[
          'w-6 font-bold text-[#bb88ff]',
          { 'cursor-pointer underline': onClickTag },
        ]"
        @click="onClickTag?.(item.id)"
        >{{ item.count }}</span
      >
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
