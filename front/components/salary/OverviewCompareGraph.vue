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
import CompareBarGraph from "~/components/common/graph/CompareBar.vue";
import type { DataSet as CompareBarGraphDataSet } from "~/components/common/graph/CompareBar.vue";

defineProps<{
  datasets: CompareBarGraphDataSet[];
  yAxisMax?: number;
}>();

const compareDataHeaders = [
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec",
  "Jan",
  "Feb",
  "Mar",
];
</script>
