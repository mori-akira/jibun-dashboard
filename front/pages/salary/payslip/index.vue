<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Salary', iconName: 'tabler:report-money', link: '/salary' },
        { text: 'Payslip', iconName: 'tabler:align-box-left-top' },
      ]"
    />

    <div class="flex justify-between">
      <Panel panel-class="w-full">
        <MonthPickerFromTo
          label="Scope"
          :date-from="dateFrom"
          :date-to="dateTo"
          label-class="w-20 font-cursive"
          pickers-wrapper-class="min-w-96 w-1/2"
          @change:from="onChangeDateFrom"
          @change:to="onChangeDateTo"
        />
      </Panel>
    </div>

    <template
      v-for="year in getFinancialYears(
        salaryStore.salaries ?? [],
        financialYearStartMonth
      ).toReversed()"
      :key="`fy-${year}`"
    >
      <div class="flex justify-between">
        <Panel panel-class="w-full overflow-x-auto">
          <h3>
            <span class="font-cursive font-bold ml-2">{{ `FY${year}` }}</span>
          </h3>
          <template
            v-for="(chunk, i) in chunkArray(
              filterSalaryByFinancialYear(
                salaryStore.salaries ?? [],
                year,
                financialYearStartMonth
              ).toReversed(),
              3
            )"
            :key="`chunk-${year}-${i}`"
          >
            <div class="h-128 flex justify-between items-center px-4 py-2">
              <template v-for="salary in chunk" :key="salary.salaryId">
                <Payslip
                  :salary="salary"
                  wrapper-class="w-full h-full"
                  title-class="font-cursive font-bold"
                  headline-class="font-cursive"
                  label-class="font-cursive"
                />
              </template>
            </div>
          </template>
        </Panel>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import MonthPickerFromTo from "~/components/common/MonthPickerFromTo.vue";
import Payslip from "~/components/salary/Payslip.vue";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import { chunkArray } from "~/utils/array";
import { withErrorHandling } from "~/utils/api-call";
import { getFinancialYears, filterSalaryByFinancialYear } from "~/utils/salary";

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

onMounted(async () => {
  await fetchSalary();
});

const financialYearStartMonth = computed(
  () => settingStore.setting?.salary.financialYearStartMonth ?? 1
);

const fetchSalary = async () => {
  withErrorHandling(
    () => salaryStore.fetchSalary(undefined, dateFrom.value, dateTo.value),
    commonStore
  );
};

const dateFrom = ref<string | undefined>("");
const onChangeDateFrom = async (value: string | undefined) => {
  dateFrom.value = value;
  await fetchSalary();
};

const dateTo = ref<string | undefined>("");
const onChangeDateTo = async (value: string | undefined) => {
  dateTo.value = value;
  await fetchSalary();
};
</script>
