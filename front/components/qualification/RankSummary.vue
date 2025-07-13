<template>
  <div :class="['wrapper', wrapperClass]">
    <div
      v-for="(value, key, index) in counter"
      :key="index"
      class="summary-item"
    >
      <span
        :class="['label']"
        :style="{color: getRankColorHexCode(key, settingStore.setting?.qualification as SettingQualification)}"
        >{{ key }} :</span
      >
      <span
        :class="['value', { 'can-navigate': canNavigate }]"
        :style="{color: getRankColorHexCode(key, settingStore.setting?.qualification as SettingQualification)}"
        @click="onClickRank(key)"
        >{{ value }}</span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Qualification, SettingQualification } from "~/api/client";
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

<style lang="css" scoped>
.wrapper {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 1rem;
}

.label {
  font-size: 1.25rem;
}

.value {
  margin-left: 0.5rem;
  font-size: 1.25rem;
  font-weight: bold;
}

.value.can-navigate {
  cursor: pointer;
  text-decoration: underline;
}
</style>
