<template>
  <div>
    <Breadcrumb :items="[{ text: 'Home', iconName: 'tabler:home' }]" />

    <div class="flex justify-between">
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
      <Panel panel-class="w-full items-center">
        <h3>
          <Icon name="tabler:certificate" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Qualification</span>
        </h3>
        <div class="h-36 flex justify-center items-center">
          <RankSummary
            :qualifications="qualificationStore.qualifications ?? []"
            wrapper-class="w-full"
            can-navigate
          />
        </div>
      </Panel>
    </div>

    <div class="flex justify-between">
      <Panel panel-class="w-full">
        <h3>
          <Icon name="tabler:book" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Vocabulary</span>
        </h3>
        <div class="h-36 flex items-center"></div>
      </Panel>
      <Panel panel-class="w-full items-center">
        <h3>
          <Icon name="tabler:report-money" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Financial Asset</span>
        </h3>
        <div class="h-36 flex justify-center items-center"></div>
      </Panel>
    </div>

    <div class="flex justify-between">
      <Panel panel-class="w-full">
        <h3>
          <Icon name="tabler:file-pencil" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Study Plan</span>
        </h3>
        <div class="h-36 flex items-center"></div>
      </Panel>
      <Panel panel-class="w-full items-center">
        <h3>
          <Icon name="tabler:settings" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Setting</span>
        </h3>
        <div class="h-36 flex justify-center items-center"></div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import type { Salary } from "~/api/client";
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import { useQualificationStore } from "~/stores/qualification";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import { withErrorHandling } from "~/utils/api-call";

const commonStore = useCommonStore();
const userStore = useUserStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();
const qualificationStore = useQualificationStore();

onMounted(async () => {
  await withErrorHandling(async () => {
    await Promise.all([
      userStore.fetchUser(),
      settingStore.fetchSetting(),
      qualificationStore.fetchQualification(),
      salaryStore.fetchSalary(),
    ]);
  }, commonStore);
});
</script>
