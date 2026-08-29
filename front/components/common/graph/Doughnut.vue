<template>
  <div :class="['relative', wrapperClass]">
    <ClientOnly>
      <DoughnutChart :data="chartData" :options="chartOptions" />
    </ClientOnly>
    <div
      v-if="centerLabel || centerValue"
      class="absolute inset-0 flex flex-col justify-center items-center pointer-events-none"
    >
      <span v-if="centerLabel" class="font-cursive text-xs text-gray-500">{{
        centerLabel
      }}</span>
      <span v-if="centerValue" class="font-bold text-sm">{{
        centerValue
      }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Doughnut as DoughnutChart } from "vue-chartjs";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  type TooltipItem,
} from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

const props = defineProps<{
  labels: string[];
  values: number[];
  colors: string[];
  valueFormat?: (value: number) => string;
  centerLabel?: string;
  centerValue?: string;
  showLegend?: boolean;
  wrapperClass?: string;
}>();

const total = computed(() =>
  props.values.reduce((sum, value) => sum + value, 0),
);

// 隣接する扇を背景色の隙間で分離する
const chartData = computed(() => ({
  labels: props.labels,
  datasets: [
    {
      data: props.values,
      backgroundColor: props.colors,
      borderColor: "#fff",
      borderWidth: 2,
    },
  ],
}));

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  cutout: "58%",
  plugins: {
    legend: {
      display: props.showLegend ?? false,
    },
    tooltip: {
      callbacks: {
        label: (context: TooltipItem<"doughnut">) => {
          const value = context.parsed ?? 0;
          const share = total.value === 0 ? 0 : (value / total.value) * 100;
          const formatted = props.valueFormat
            ? props.valueFormat(value)
            : value.toLocaleString();
          return ` ${context.label}: ${formatted} (${Number(share.toFixed(1))}%)`;
        },
      },
    },
  },
  animation: {
    duration: 0,
  },
}));
</script>
