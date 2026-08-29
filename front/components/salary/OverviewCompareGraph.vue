<template>
  <div class="h-48 flex items-center">
    <CompareBarGraph
      :labels="compareDataHeaders"
      :datasets="datasets"
      :y-axis-min="0"
      :y-axis-max="yAxisMax"
      :show-y-grid="true"
      wrapper-class="w-full h-36 mt-4"
    />
  </div>
</template>

<script setup lang="ts">
import { useSettingStore } from "~/stores/setting";
import CompareBarGraph from "~/components/common/graph/CompareBar.vue";
import type { DataSet } from "~/components/common/graph/types";
import { getMonthLabelsInFinancialYear } from "~/utils/salary";

defineProps<{
  datasets: DataSet[];
  yAxisMax?: number;
}>();

const settingStore = useSettingStore();

const compareDataHeaders = computed(() =>
  getMonthLabelsInFinancialYear(
    settingStore.setting?.salary.financialYearStartMonth ?? 1,
  ),
);
</script>
