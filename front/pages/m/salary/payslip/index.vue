<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Salary', iconName: 'tabler:report-money', link: '/m/salary' },
        { text: 'Payslip', iconName: 'tabler:align-box-left-top' },
      ]"
    />

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <SelectBox
          label="Financial Year"
          :options="
            financialYears
              .map((year) => ({
                label: year,
                value: year,
              }))
              .toReversed()
          "
          :value="targetFinancialYear"
          wrapper-class="items-center"
          label-class="font-cursive"
          @change:value="targetFinancialYear = $event"
        />
      </Panel>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2 h-[calc(100vh-13rem)] overflow-y-auto">
        <Payslip
          v-for="salary in filterSalaryByFinancialYear(
            salaryStore.salaries ?? [],
            targetFinancialYear,
            financialYearStartMonth
          ).toReversed()"
          :key="salary.salaryId"
          :salary="salary"
          wrapper-class="!min-w-72 h-[calc(100vh-16rem)] overflow-y-auto"
          title-class="font-cursive font-bold"
          headline-class="font-cursive"
          label-class="font-cursive"
        />
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import Payslip from "~/components/salary/Payslip.vue";

import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";

definePageMeta({
  layout: "mobile",
});

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
  await withErrorHandling(
    async () => await salaryStore.fetchSalary(undefined),
    commonStore
  );
};

const financialYears = computed(() =>
  getFinancialYears(salaryStore.salaries ?? [], financialYearStartMonth.value)
);
const targetFinancialYear = ref(financialYears.value.at(-1) ?? "");
</script>
