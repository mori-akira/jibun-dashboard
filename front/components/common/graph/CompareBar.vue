<template>
  <div :class="[wrapperClass]">
    <ClientOnly>
      <Bar :data="chartData" :options="chartOptions" />
    </ClientOnly>
  </div>
</template>

<script setup lang="ts">
import { Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

export type DataSet = {
  label: string;
  data: number[];
  backgroundColor: string;
};

const props = defineProps<{
  labels: string[];
  datasets: DataSet[];
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
  datasets: props.datasets,
}));

const chartOptions = {
  responsive: true,
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
  animation: {
    duration: 0,
  },
};
</script>
