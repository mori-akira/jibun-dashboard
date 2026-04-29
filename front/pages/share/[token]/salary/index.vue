<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[{ text: 'Salary', iconName: 'tabler:report-money' }]"
      />
      <div class="flex items-center mr-4">
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-32"
          @click:button="() => navigateTo(`/share/${token}/salary/payslip`)"
        >
          <Icon
            name="tabler:align-box-left-top"
            class="text-base translate-y-0.5"
          />
          <span class="font-cursive ml-2">Payslip</span>
        </Button>
      </div>
    </div>

    <div
      v-if="status === 'forbidden'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:lock" class="text-5xl" />
      <p>This share link does not include salary data.</p>
    </div>

    <div
      v-else-if="status === 'gone'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:clock-off" class="text-5xl" />
      <p>This share link has expired.</p>
    </div>

    <template v-else>
      <div class="flex justify-between">
        <Panel wrapper-class="w-full">
          <SelectBox
            v-model="baseFinancialYear"
            label="Base Financial Year"
            :options="
              financialYears
                .map((year) => ({
                  label: year,
                  value: year,
                }))
                .toReversed()
            "
            wrapper-class="items-center"
            label-class="font-cursive"
          />
        </Panel>
      </div>

      <div class="flex justify-between">
        <Panel wrapper-class="w-5/12">
          <h3>
            <Icon name="tabler:coin-yen" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Annual Income</span>
          </h3>
          <div class="h-36 flex items-center">
            <AnnualComparer
              :selector="(salary: Salary) => salary.overview.grossIncome + salary.overview.bonus"
              :value-format="(value: number) => `￥${value.toLocaleString()}`"
              :base-financial-year="baseFinancialYear"
              :financial-year-start-month="financialYearStartMonth"
              wrapper-class="w-full"
            />
          </div>
        </Panel>
        <Panel wrapper-class="w-7/12">
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

      <div class="flex justify-between">
        <Panel wrapper-class="w-5/12">
          <h3>
            <Icon name="tabler:stopwatch" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Annual Overtime</span>
          </h3>
          <div class="h-36 flex items-center">
            <AnnualComparer
              :selector="(salary: Salary) => salary.overview.overtime"
              :value-format="(value: number) => `${value.toLocaleString()} H`"
              :base-financial-year="baseFinancialYear"
              :financial-year-start-month="financialYearStartMonth"
              positive-color-text-class="text-red-600"
              negative-color-text-class="text-blue-600"
              positive-color-background-class="bg-red-600"
              negative-color-background-class="bg-blue-600"
              wrapper-class="w-full"
            />
          </div>
        </Panel>
        <Panel wrapper-class="w-7/12">
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

      <div class="flex justify-between">
        <Panel wrapper-class="w-9/12">
          <h3>
            <Icon name="tabler:clipboard-data" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Compare</span>
          </h3>
          <Tabs
            :tabs="[
              { label: 'Gross Income', slot: 'grossIncome' },
              { label: 'Net Income', slot: 'netIncome' },
              { label: 'Operating Time', slot: 'operatingTime' },
              { label: 'Overtime', slot: 'overtime' },
              { label: 'Bonus', slot: 'bonus' },
            ]"
            :init-tab="selectedTab"
            button-class="font-cursive"
            @change:tab="onChangeTab"
          >
            <template #grossIncome>
              <div class="mt-4">
                <OverviewCompareGraph
                  :datasets="compareData.grossIncome"
                  :y-axis-max="incomeMaxRange"
                />
              </div>
            </template>
            <template #netIncome>
              <div class="mt-4">
                <OverviewCompareGraph
                  :datasets="compareData.netIncome"
                  :y-axis-max="incomeMaxRange"
                />
              </div>
            </template>
            <template #operatingTime>
              <div class="mt-4">
                <OverviewCompareGraph :datasets="compareData.operatingTime" />
              </div>
            </template>
            <template #overtime>
              <div class="mt-4">
                <OverviewCompareGraph :datasets="compareData.overtime" />
              </div>
            </template>
            <template #bonus>
              <div class="mt-4">
                <OverviewCompareGraph :datasets="compareData.bonus" />
              </div>
            </template>
          </Tabs>
        </Panel>
        <Panel wrapper-class="w-3/12">
          <OverviewCompareSummary :summary-data="summaryData" />
        </Panel>
      </div>

      <div class="flex justify-between">
        <Panel wrapper-class="w-full overflow-x-auto">
          <h3>
            <Icon name="tabler:align-box-left-top" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Payslip</span>
          </h3>
          <div class="h-128 flex justify-between items-center px-4 py-2">
            <template
              v-for="(salary, i) in padArray(
                trimArray(
                  filterSalaryByFinancialYears(
                    salaryStore.salaries ?? [],
                    targetFinancialYears,
                    financialYearStartMonth,
                  ).toReversed(),
                  3,
                ),
                3,
                undefined,
              )"
              :key="salary?.salaryId ?? `empty-${i}`"
            >
              <Payslip
                v-if="salary"
                :salary="salary"
                wrapper-class="w-full h-full"
                title-class="font-cursive font-bold"
                headline-class="font-cursive"
                label-class="font-cursive"
              />
              <div v-if="!salary" class="min-w-82 w-full h-full m-4"></div>
            </template>
          </div>
          <div class="h-10 flex justify-end items-end px-4">
            <AnchorLink
              :link="`/share/${token}/salary/payslip`"
              text="View All Payslips"
              target="_self"
              anchor-class="font-cursive"
            />
          </div>
        </Panel>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { AxiosError } from "axios";
import type { Ref } from "vue";
import type { Overview, Salary, Setting } from "~/generated/api/client";
import { useApiClient } from "~/composables/common/useApiClient";
import { useSalaryStore } from "~/stores/salary";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Tabs from "~/components/common/Tabs.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import AnchorLink from "~/components/common/AnchorLink.vue";
import Button from "~/components/common/Button.vue";
import TransitionGraph from "~/components/common/graph/Transition.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import OverviewCompareGraph from "~/components/salary/OverviewCompareGraph.vue";
import OverviewCompareSummary from "~/components/salary/OverviewCompareSummary.vue";
import Payslip from "~/components/salary/Payslip.vue";
import {
  getFinancialYears,
  filterFinancialYears,
  filterSalaryByFinancialYears,
  aggregateAnnually,
  aggregateCompareData,
} from "~/utils/salary";
import { trimArray, padArray } from "~/utils/array";

definePageMeta({ layout: "share" });

const route = useRoute();
const token = route.params.token as string;
const { getShareApi } = useApiClient();
const salaryStore = useSalaryStore();

const status = ref<"ok" | "forbidden" | "gone">("ok");
const shareSetting = ref<Setting | null>(null);
const shareStatus = inject<Ref<"ok" | "gone">>("shareStatus")!;
const forbiddenTypes = inject<Ref<string[]>>("forbiddenTypes")!;

onMounted(async () => {
  try {
    const [salaryRes, settingRes] = await Promise.all([
      getShareApi().getShareSalaries(token),
      getShareApi().getShareSetting(token),
    ]);
    salaryStore.salaries = salaryRes.data;
    shareSetting.value = settingRes.data;
  } catch (err) {
    if (err instanceof AxiosError) {
      if (err.response?.status === 403) {
        status.value = "forbidden";
        forbiddenTypes.value = [...forbiddenTypes.value, "salary"];
      } else if (err.response?.status === 410) status.value = "gone";
    }
  }
  if (status.value === "gone") shareStatus.value = "gone";
});

const financialYearStartMonth = computed(
  () => shareSetting.value?.salary.financialYearStartMonth ?? 1,
);
const transitionItemCount = computed(
  () => shareSetting.value?.salary.transitionItemCount ?? 1,
);
const compareDataColors = computed(() => {
  const colors = shareSetting.value?.salary.compareDataColors ?? [];
  return [colors?.[0] ?? "#ddd", colors?.[1] ?? "#ddd", colors?.[2] ?? "#ddd"];
});

const financialYears = computed(() =>
  getFinancialYears(salaryStore.salaries ?? [], financialYearStartMonth.value),
);
const baseFinancialYear = ref(financialYears.value.at(-1) ?? "");
watch(
  () => financialYears.value,
  () => (baseFinancialYear.value = financialYears.value.at(-1) ?? ""),
);
const targetFinancialYears = computed(() =>
  filterFinancialYears(financialYears.value, baseFinancialYear.value),
);

const aggregateOverviewAnnually = (key: keyof Overview) =>
  trimArray(
    targetFinancialYears.value,
    shareSetting.value?.salary.transitionItemCount ?? 1,
    { from: "end" },
  ).map((year) =>
    aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview?.[key],
      year,
      financialYearStartMonth.value,
    ),
  );
const annualIncomes = computed(() => {
  const grossIncomes = aggregateOverviewAnnually("grossIncome");
  const bonuses = aggregateOverviewAnnually("bonus");
  return grossIncomes.map((val, index) => val + (bonuses?.[index] ?? 0));
});
const annualOvertime = computed(() => aggregateOverviewAnnually("overtime"));

type TabSlot =
  | "grossIncome"
  | "netIncome"
  | "operatingTime"
  | "overtime"
  | "bonus";
const aggregateCompareDataWrapper = (key: TabSlot) =>
  aggregateCompareData(
    salaryStore.salaries ?? [],
    trimArray(targetFinancialYears.value, 3, { from: "end" }),
    key,
    compareDataColors.value,
    financialYearStartMonth.value,
  );
const compareData = computed(() => ({
  grossIncome: aggregateCompareDataWrapper("grossIncome"),
  netIncome: aggregateCompareDataWrapper("netIncome"),
  operatingTime: aggregateCompareDataWrapper("operatingTime"),
  overtime: aggregateCompareDataWrapper("overtime"),
  bonus: aggregateCompareDataWrapper("bonus"),
}));
const incomeMaxRange = computed(() => {
  const maxValue = compareData.value.grossIncome.reduce(
    (max: number, dataset) => Math.max(max, ...dataset.data),
    0,
  );
  return Math.ceil(maxValue / 100000) * 100000;
});
const selectedTab = ref<TabSlot>("grossIncome");
const onChangeTab = (slot: string) => {
  selectedTab.value = slot as TabSlot;
};
const summaryData = computed<{ label: string; value: number }[]>(() => {
  const targetData = compareData.value[selectedTab.value];
  return targetData.toReversed().map((dataset) => ({
    label: dataset.label,
    value: dataset.data.reduce((sum, value) => sum + value, 0),
  }));
});
</script>
