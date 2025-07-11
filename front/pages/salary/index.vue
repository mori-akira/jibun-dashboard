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
        <div class="w-full mt-4 ml-2">
          <span class="inline-block font-cursive w-1/2">This Year</span>
          <span
            :class="[
              'inline-block',
              'font-bold',
              'w-1/2',
              { 'text-blue-600': yearIncome.thisYear >= yearIncome.lastYear },
              { 'text-red-600': yearIncome.thisYear < yearIncome.lastYear },
            ]"
            >{{ `￥${yearIncome.thisYear.toLocaleString()}` }}</span
          >
        </div>
        <CompareBar
          v-if="yearIncome.thisYear >= yearIncome.lastYear"
          :left-value="yearIncome.lastYear"
          :right-value="yearIncome.thisYear - yearIncome.lastYear"
          wrapper-class="mt-4"
          left-class="bg-gray-600"
          right-class="bg-blue-600"
        />
        <CompareBar
          v-else-if="yearIncome.thisYear < yearIncome.lastYear"
          :left-value="yearIncome.thisYear"
          :right-value="yearIncome.lastYear - yearIncome.thisYear"
          wrapper-class="mt-4 ml-2 mr-2"
          left-class="bg-red-600"
          right-class="bg-gray-600"
        />
        <div class="w-full mt-4 ml-2">
          <span class="inline-block font-cursive w-1/2">Last Year</span>
          <span class="inline-block font-bold w-1/2">{{
            `￥${yearIncome.lastYear.toLocaleString()}`
          }}</span>
        </div>
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
import CompareBar from "~/components/common/CompareBar.vue";
import { getYears, getTotalYearIncome } from "~/utils/salary";

const commonStore = useCommonStore();
const salaryStore = useSalaryStore();

const years = ref<string[]>([]);
const thisYear = ref<string>("");
const lastYear = ref<string>("");
const yearIncome = ref({ thisYear: 0, lastYear: 0 });

onMounted(async () => {
  commonStore.setLoading(true);
  await salaryStore.fetchSalary();
  commonStore.setLoading(false);
});

watch(
  () => salaryStore.salaries,
  () => {
    years.value = getYears(salaryStore?.salaries ?? []);
    thisYear.value =
      years.value.length > 0 ? years.value[years.value.length - 1] : "";
    lastYear.value =
      years.value.length > 1 ? years.value[years.value.length - 2] : "";
    yearIncome.value.thisYear = getTotalYearIncome(
      salaryStore?.salaries ?? [],
      thisYear.value
    );
    yearIncome.value.lastYear = getTotalYearIncome(
      salaryStore?.salaries ?? [],
      lastYear.value
    );
  },
  { immediate: true }
);
</script>

<style lang="css" scoped>
.row {
  width: 100%;
  margin-top: 1rem;
}
</style>
