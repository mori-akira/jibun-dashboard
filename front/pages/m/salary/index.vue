<template>
  <div>
    <div class="flex-1 w-full">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:report-money" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Salary</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.grossIncome + salary.overview.bonus"
            :value-format="(value: number) => `￥${value.toLocaleString()}`"
            :base-financial-year="baseFinancialYear"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
    </div>
    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:graph" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
        <div class="h-36 flex items-center">
          <TransitionGraph
            :labels="
              trimArray(targetFinancialYears, transitionItemCount, {
                from: 'end',
              })
            "
            :values="
              trimArray(annualIncomes, transitionItemCount, { from: 'end' })
            "
            :y-axis-min="0"
            :show-y-grid="true"
            wrapper-class="w-full h-36 mt-4"
          />
        </div>
      </Panel>
    </div>
    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:stopwatch" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Annual Overtime</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.overtime"
            :value-format="(value: number) => `${value.toLocaleString()} H`"
            :base-financial-year="baseFinancialYear"
            positive-color-text-class="text-red-600"
            negative-color-text-class="text-blue-600"
            positive-color-background-class="bg-red-600"
            negative-color-background-class="bg-blue-600"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
    </div>
    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:graph" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
        <div class="h-36 flex items-center">
          <TransitionGraph
            :labels="
              trimArray(targetFinancialYears, transitionItemCount, {
                from: 'end',
              })
            "
            :values="
              trimArray(annualOvertime, transitionItemCount, { from: 'end' })
            "
            :y-axis-min="0"
            :show-y-grid="true"
            wrapper-class="w-full h-36 mt-4"
          />
        </div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Overview, Salary } from "~/generated/api/client";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import Panel from "~/components/common/Panel.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import TransitionGraph from "~/components/common/graph/Transition.vue";
import { withErrorHandling } from "~/utils/api-call";

definePageMeta({
  layout: "mobile",
});

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

onMounted(async () => {
  await withErrorHandling(async () => {
    await Promise.all([salaryStore.fetchSalary()]);
  }, commonStore);
});

const financialYearStartMonth = computed(
  () => settingStore.setting?.salary.financialYearStartMonth ?? 1
);
const transitionItemCount = computed(() =>
  Math.min(settingStore.setting?.salary.transitionItemCount ?? 1, 4)
);
const financialYears = computed(() =>
  getFinancialYears(salaryStore.salaries ?? [], financialYearStartMonth.value)
);
const baseFinancialYear = ref(financialYears.value.at(-1) ?? "");
watch(
  () => financialYears.value,
  () => (baseFinancialYear.value = financialYears.value.at(-1) ?? "")
);

const targetFinancialYears = computed(() =>
  filterFinancialYears(financialYears.value, baseFinancialYear.value)
);
const aggregateOverviewAnnually = (key: keyof Overview) =>
  trimArray(
    targetFinancialYears.value,
    Math.min(settingStore.setting?.salary.transitionItemCount ?? 1, 4),
    { from: "end" }
  ).map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview?.[key],
      year,
      financialYearStartMonth.value
    );
  });
const annualIncomes = computed(() => {
  const grossIncomes = aggregateOverviewAnnually("grossIncome");
  const bonuses = aggregateOverviewAnnually("bonus");
  return grossIncomes.map((val, index) => val + (bonuses?.[index] ?? 0));
});
const annualOvertime = computed(() => aggregateOverviewAnnually("overtime"));
</script>
