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
import type { TooltipItem, ChartEvent, ActiveElement } from "chart.js";
import type { DataSet } from "./types";

export type { DataSet };

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
);

const props = defineProps<{
  labels: string[];
  datasets: DataSet[];
  stacked?: boolean;
  valueFormat?: (value: number) => string;
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

const emits = defineEmits<{
  (e: "click:label", index: number): void;
}>();

// 積み上げ時は隣接する区間を背景色の隙間で分離し、最上段のみ角を丸める
const chartData = computed(() => ({
  labels: props.labels,
  datasets: props.stacked
    ? props.datasets.map((dataset, index) => ({
        ...dataset,
        borderColor: "#fff",
        borderWidth: 2,
        borderSkipped: false,
        borderRadius: index === props.datasets.length - 1 ? 4 : 0,
      }))
    : props.datasets,
}));

const formatValue = (value: number) =>
  props.valueFormat ? props.valueFormat(value) : value.toLocaleString();

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: {
      stacked: props.stacked ?? false,
      grid: {
        display: props.showXGrid ?? false,
      },
      min: props.xAxisMin,
      max: props.xAxisMax,
    },
    y: {
      stacked: props.stacked ?? false,
      grid: {
        display: props.showYGrid ?? false,
      },
      min: props.yAxisMin,
      max: props.yAxisMax,
      ticks: props.valueFormat
        ? { callback: (value: string | number) => formatValue(Number(value)) }
        : undefined,
    },
  },
  plugins: {
    legend: {
      display: props.showLegend ?? false,
    },
    title: {
      display: props.showTitle ?? false,
    },
    tooltip: {
      callbacks: {
        label: (context: TooltipItem<"bar">) =>
          ` ${context.dataset.label}: ${formatValue(context.parsed.y ?? 0)}`,
      },
    },
  },
  animation: {
    duration: 0,
  },
  // バーそのものだけでなく、その月の列全体をクリック対象にする
  onClick: (event: ChartEvent, _elements: ActiveElement[], chart: ChartJS) => {
    if (!event.native) {
      return;
    }
    const points = chart.getElementsAtEventForMode(
      event.native,
      "index",
      { intersect: false },
      true,
    );
    const index = points[0]?.index;
    if (index !== undefined) {
      emits("click:label", index);
    }
  },
}));
</script>
