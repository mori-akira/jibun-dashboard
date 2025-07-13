<template>
  <div class="w-full h-full flex flex-col justify-around items-center p-4">
    <div
      v-for="(data, index) in summaryData"
      :key="index"
      class="w-full flex justify-center"
      :style="{ color: compareDataColors.toReversed()[index] }"
    >
      <span class="font-cursive">{{ data.label }}</span>
      <span class="font-bold ml-4">
        {{ data.value.toLocaleString() }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSettingStore } from "~/stores/setting";

defineProps<{
  summaryData: { label: string; value: number }[];
}>();

const settingStore = useSettingStore();
const compareDataColors = computed(() => {
  const colors = settingStore.setting?.salary.compareDataColors ?? [];
  return [colors?.[0] ?? "#ddd", colors?.[1] ?? "#ddd", colors?.[2] ?? "#ddd"];
});
</script>
