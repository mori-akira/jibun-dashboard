<template>
  <div>
    <CompareBarGraph
      :labels="monthLabels"
      :datasets="datasets"
      :stacked="true"
      :value-format="formatAmount"
      :y-axis-min="0"
      :show-y-grid="true"
      :wrapper-class="['cursor-pointer', wrapperClass]"
      @click:label="(index: number) => emits('click:month', index)"
    />
    <div class="flex flex-wrap justify-center gap-x-6 gap-y-1 mt-4 px-2">
      <div
        v-for="item in visibleSummary"
        :key="item.key"
        class="flex items-center text-sm"
      >
        <span
          class="inline-block w-3 h-3 rounded-[2px] mr-2"
          :style="{ backgroundColor: item.color }"
        ></span>
        <span class="font-cursive">{{ item.label }}</span>
        <span class="font-bold ml-2">￥{{ item.total.toLocaleString() }}</span>
        <span class="ml-1 text-gray-500">({{ item.share.toFixed(1) }}%)</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { StructureSummary } from "~/utils/salary";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import CompareBarGraph from "~/components/common/graph/CompareBar.vue";
import {
  getMonthLabelsInFinancialYear,
  aggregateStructureData,
  buildMonthlyColors,
} from "~/utils/salary";

const props = defineProps<{
  summary: StructureSummary[];
  baseFinancialYear?: string;
  financialYearStartMonth?: number;
  /** 選択中の月(年度開始月からの 0 始まりの位置)。null なら未選択 */
  selectedMonthIndex?: number | null;
  wrapperClass?: string;
}>();

const emits = defineEmits<{
  (e: "click:month", index: number): void;
}>();

const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

const financialYearStartMonth = computed(
  () =>
    props.financialYearStartMonth ??
    settingStore.setting?.salary.financialYearStartMonth ??
    1,
);
const monthLabels = computed(() =>
  getMonthLabelsInFinancialYear(financialYearStartMonth.value),
);

// 選択中の月以外は減光して選択状態を示す
// 未選択時も色は配列で渡す(buildMonthlyColors の注記を参照)
const datasets = computed(() =>
  aggregateStructureData(
    salaryStore.salaries ?? [],
    props.baseFinancialYear ?? "",
    financialYearStartMonth.value,
  ).map((dataset) => {
    const color = Array.isArray(dataset.backgroundColor)
      ? (dataset.backgroundColor[0] ?? "#ddd")
      : dataset.backgroundColor;
    return {
      ...dataset,
      backgroundColor: buildMonthlyColors(
        color,
        monthLabels.value.length,
        props.selectedMonthIndex,
      ),
    };
  }),
);

// 値のない項目は凡例に出さない
const visibleSummary = computed(() =>
  props.summary.filter((item) => item.total !== 0),
);

const formatAmount = (value: number) => `￥${value.toLocaleString()}`;
</script>
