<template>
  <div>
    <Breadcrumb :items="[{ text: 'Home', iconName: 'tabler:home' }]" />

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full">
        <h3>
          <Icon name="tabler:report-money" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Salary</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.grossIncome + salary.overview.bonus"
            :value-format="(value: number) => `￥${value.toLocaleString()}`"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full items-center">
        <h3>
          <Icon name="tabler:certificate" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Qualification</span>
        </h3>
        <div class="h-36 flex justify-center items-center">
          <RankSummary
            :qualifications="qualificationStore.qualifications ?? []"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Salary } from "~/generated/api/client";
import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import { useQualificationStore } from "~/stores/qualification";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import { withErrorHandling } from "~/utils/api-call";

definePageMeta({
  layout: "mobile",
});

const commonStore = useCommonStore();
const salaryStore = useSalaryStore();
const qualificationStore = useQualificationStore();

onMounted(async () => {
  await withErrorHandling(async () => {
    await Promise.all([
      qualificationStore.fetchQualification(),
      salaryStore.fetchSalary(),
    ]);
  }, commonStore);
});
</script>
