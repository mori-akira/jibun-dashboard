<template>
  <div :class="[wrapperClass, 'mt-4']">
    <ClientOnly>
      <Line
        :data="chartData"
        :options="chartOptions"
        :style="{ height: '300px' }"
      />
    </ClientOnly>
  </div>
</template>

<script setup lang="ts">
import { Line } from "vue-chartjs";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
} from "chart.js";

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement
);

const props = defineProps<{
  labels: string[];
  values: number[];
  fill?: boolean;
  borderColor?: string;
  backgroundColor?: string;
  showXGrid?: boolean;
  showYGrid?: boolean;
  xAxisMin?: number;
  xAxisMax?: number;
  yAxisMin?: number;
  yAxisMax?: number;
  showLegend?: boolean;
  showTitle?: boolean;
  wrapperClass?: string;
}>();

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [
    {
      data: props.values,
      fill: props.fill ?? false,
      borderColor: props.borderColor ?? "#33DD88",
      backgroundColor: props.backgroundColor ?? "#33DD8880",
    },
  ],
}));
const chartOptions = {
  maintainAspectRatio: false,
  scales: {
    x: {
      grid: {
        display: props.showXGrid ?? false,
      },
      min: props.xAxisMin,
      max: props.xAxisMax,
    },
    y: {
      grid: {
        display: props.showYGrid ?? false,
      },
      min: props.yAxisMin,
      max: props.yAxisMax,
    },
  },
  plugins: {
    legend: {
      display: props.showLegend ?? false,
    },
    title: {
      display: props.showTitle ?? false,
    },
  },
};
</script>

<style lang="css" scoped>
canvas {
  height: unset !important;
}
</style>
