<template>
  <div
    :class="[
      'w-full h-full flex flex-col justify-center items-center p-2',
      wrapperClass,
    ]"
  >
    <!-- canvas の実寸がパネルの最小幅を押し広げないよう overflow で切る -->
    <div class="w-full overflow-hidden">
      <Doughnut
        :labels="summary.map((item) => item.label)"
        :values="summary.map((item) => item.total)"
        :colors="summary.map((item) => item.color)"
        :value-format="formatAmount"
        :center-label="selectedMonthLabel ?? `FY${baseFinancialYear}`"
        :center-value="formatAmount(summaryTotal)"
        wrapper-class="w-full h-48"
      />
    </div>
    <p class="mt-2 text-xs text-gray-500 text-center">
      {{
        selectedMonthIndex == null
          ? "Click a month to focus"
          : "Click the month again to clear"
      }}
    </p>
  </div>
</template>

<script setup lang="ts">
import type { StructureSummary } from "~/utils/salary";
import { useSettingStore } from "~/stores/setting";
import Doughnut from "~/components/common/graph/Doughnut.vue";
import { getMonthLabelsInFinancialYear } from "~/utils/salary";

const props = defineProps<{
  summary: StructureSummary[];
  baseFinancialYear?: string;
  financialYearStartMonth?: number;
  /** 選択中の月(年度開始月からの 0 始まりの位置)。null なら年度合計を表示 */
  selectedMonthIndex?: number | null;
  wrapperClass?: string;
}>();

const settingStore = useSettingStore();

const financialYearStartMonth = computed(
  () =>
    props.financialYearStartMonth ??
    settingStore.setting?.salary.financialYearStartMonth ??
    1,
);
const selectedMonthLabel = computed(() =>
  props.selectedMonthIndex == null
    ? null
    : (getMonthLabelsInFinancialYear(financialYearStartMonth.value)[
        props.selectedMonthIndex
      ] ?? null),
);
const summaryTotal = computed(() =>
  props.summary.reduce((total, item) => total + item.total, 0),
);

const formatAmount = (value: number) => `￥${value.toLocaleString()}`;
</script>
