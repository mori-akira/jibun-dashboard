<template>
  <div :class="['flex justify-around items-center p-4', wrapperClass]">
    <div
      v-for="(value, key, index) in counter"
      :key="index"
      class="summary-item"
    >
      <span
        class="text-xl"
        :style="{ color: getRankColorHexCode(key, qualificationSetting) }"
        >{{ key }} :</span
      >
      <span
        :class="[
          'ml-2 text-xl font-bold',
          { 'cursor-pointer underline': canNavigate },
        ]"
        :style="{ color: getRankColorHexCode(key, qualificationSetting) }"
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
  filter?: (qualification: Qualification) => boolean;
  settingQualification?: SettingQualification;
  wrapperClass?: string;
  canNavigate?: boolean;
}>();
const settingStore = useSettingStore();

const qualificationSetting = computed(
  () =>
    props.settingQualification ??
    (settingStore.setting?.qualification as SettingQualification),
);

const counter = computed(() => {
  const counter: Counter = {
    A: 0,
    B: 0,
    C: 0,
    D: 0,
  };
  props.qualifications
    .filter((q) => (props.filter ? props.filter(q) : true))
    .forEach((e) => counter[e.rank]++);
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
