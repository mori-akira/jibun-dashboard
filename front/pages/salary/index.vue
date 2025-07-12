<template>
  <div>
    <h2>
      <Icon name="tabler:report-money" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Salary</span>
    </h2>

    <div class="flex justify-between">
      <Panel panel-class="w-5/12">
        <h3>
          <Icon name="tabler:coin-yen" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Annual Income</span>
        </h3>
        <AnnualIncomeComparer />
      </Panel>
      <Panel panel-class="w-7/12">
        <h3>
          <Icon name="tabler:graph" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import Panel from "~/components/common/Panel.vue";
import AnnualIncomeComparer from "~/components/salary/AnnualIncomeComparer.vue";
// import { getYears, getTotalYearIncome } from "~/utils/salary";

const commonStore = useCommonStore();
const salaryStore = useSalaryStore();

// const years = computed(() => getYears(salaryStore.salaries ?? []));
// const thisYear = computed(() => years.value.at(-1) ?? "");
// const lastYear = computed(() => years.value.at(-2) ?? "");
// const yearIncome = computed(() => ({
//   thisYear: getTotalYearIncome(salaryStore.salaries ?? [], thisYear.value),
//   lastYear: getTotalYearIncome(salaryStore.salaries ?? [], lastYear.value),
// }));

onMounted(async () => {
  commonStore.setLoading(true);
  await salaryStore.fetchSalary();
  commonStore.setLoading(false);
});
</script>

<style lang="css" scoped>
.row {
  width: 100%;
  margin-top: 1rem;
}
</style>
