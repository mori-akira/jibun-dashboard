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
  wrapperClass?: string;
}>();

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [
    {
      data: props.values,
      fill: false,
      borderColor: "#33DD88",
      backgroundColor: "#33DD8880",
    },
  ],
}));
const chartOptions = {
  maintainAspectRatio: false,
  scales: {
    y: {
      min: 0,
    },
  },
  plugins: {
    legend: {
      display: false,
    },
    title: {
      display: false,
    },
  },
};
</script>

<style lang="css" scoped>
canvas {
  height: unset !important;
}
</style>
