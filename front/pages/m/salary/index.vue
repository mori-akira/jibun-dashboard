<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[{ text: 'Salary', iconName: 'tabler:report-money' }]"
      />
      <Button
        type="navigation"
        size="small"
        html-type="button"
        button-class="w-32"
        @click="() => navigateTo('/m/salary/payslip')"
      >
        <Icon
          name="tabler:align-box-left-top"
          class="text-base translate-y-0.5"
        />
        <span class="font-cursive ml-2">Payslip</span>
      </Button>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:report-money" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Annual Gross Income</span>
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
              trimArray(annualGrossIncomes, transitionItemCount, {
                from: 'end',
              })
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
          <Icon name="tabler:report-money" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Annual Net Income</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.netIncome + salary.overview.bonusTakeHome"
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
              trimArray(annualNetIncomes, transitionItemCount, {
                from: 'end',
              })
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
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
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
const annualGrossIncomes = computed(() => {
  const grossIncomes = aggregateOverviewAnnually("grossIncome");
  const bonuses = aggregateOverviewAnnually("bonus");
  return grossIncomes.map((val, index) => val + (bonuses?.[index] ?? 0));
});
const annualNetIncomes = computed(() => {
  const netIncomes = aggregateOverviewAnnually("netIncome");
  const bonusTakeHomes = aggregateOverviewAnnually("bonusTakeHome");
  return netIncomes.map((val, index) => val + (bonusTakeHomes?.[index] ?? 0));
});
const annualOvertime = computed(() => aggregateOverviewAnnually("overtime"));
</script>
