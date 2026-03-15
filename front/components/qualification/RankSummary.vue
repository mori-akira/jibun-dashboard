<template>
  <div :class="['flex justify-around items-center p-4', wrapperClass]">
    <div
      v-for="(value, key, index) in counter"
      :key="index"
      class="summary-item"
    >
      <span
        class="text-xl"
        :style="{color: getRankColorHexCode(key, settingStore.setting?.qualification as SettingQualification)}"
        >{{ key }} :</span
      >
      <span
        :class="[
          'ml-2 text-xl font-bold',
          { 'cursor-pointer underline': canNavigate },
        ]"
        :style="{color: getRankColorHexCode(key, settingStore.setting?.qualification as SettingQualification)}"
        @click="onClickRank(key)"
        >{{ value }}</span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
import type {
  Qualification,
  SettingQualification,
} from "~/generated/api/client";
import { useSettingStore } from "~/stores/setting";
import { getRankColorHexCode } from "~/utils/qualification";

type Counter = {
  A: number;
  B: number;
  C: number;
  D: number;
};

const props = defineProps<{
  qualifications: Qualification[];
  wrapperClass?: string;
  canNavigate?: boolean;
}>();
const settingStore = useSettingStore();

const counter = computed(() => {
  const counter: Counter = {
    A: 0,
    B: 0,
    C: 0,
    D: 0,
  };
  props.qualifications.forEach((e) => counter[e.rank]++);
  return counter;
});

const onClickRank = (rank: string) => {
  if (!props.canNavigate) {
    return;
  }
  navigateTo({
    path: "/qualification",
    query: {
      rank,
    },
  });
};
</script>
