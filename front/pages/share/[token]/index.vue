<template>
  <div>
    <Breadcrumb :items="[{ text: 'Home', iconName: 'tabler:home' }]" />

    <div
      v-if="status === 'gone'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:clock-off" class="text-5xl" />
      <p>このシェアリンクは期限切れです。</p>
    </div>

    <template v-else>
      <div class="flex justify-between">
        <Panel wrapper-class="w-full">
          <h3>
            <Icon name="tabler:report-money" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Salary</span>
          </h3>
          <div class="h-36 flex items-center">
            <AnnualComparer
              :selector="(salary: Salary) => salary.overview.grossIncome + salary.overview.bonus"
              :value-format="(value: number) => `￥${value.toLocaleString()}`"
              :financial-year-start-month="shareSetting?.salary.financialYearStartMonth"
              wrapper-class="w-full"
            />
          </div>
        </Panel>
        <Panel wrapper-class="w-full items-center">
          <h3>
            <Icon name="tabler:certificate" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Qualification</span>
          </h3>
          <div class="h-36 flex justify-center items-center">
            <RankSummary
              :qualifications="qualificationStore.qualifications ?? []"
              :setting-qualification="shareSetting?.qualification"
              wrapper-class="w-full"
            />
          </div>
        </Panel>
      </div>

      <div class="flex justify-between">
        <Panel wrapper-class="w-full">
          <h3>
            <Icon name="tabler:book" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Vocabulary</span>
          </h3>
          <div class="h-36 flex items-center">
            <TagCountSummary
              :vocabularies="vocabularyStore.vocabularies ?? []"
              wrapper-class="w-full"
            />
          </div>
        </Panel>
        <div class="w-full m-6" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { AxiosError } from "axios";
import type { Salary, Setting } from "~/generated/api/client";
import { useApiClient } from "~/composables/common/useApiClient";
import { useSalaryStore } from "~/stores/salary";
import { useQualificationStore } from "~/stores/qualification";
import { useVocabularyStore } from "~/stores/vocabulary";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import TagCountSummary from "~/components/vocabulary/TagCountSummary.vue";

definePageMeta({ layout: "share" });

const route = useRoute();
const token = route.params.token as string;
const { getShareApi } = useApiClient();
const salaryStore = useSalaryStore();
const qualificationStore = useQualificationStore();
const vocabularyStore = useVocabularyStore();

const status = ref<"ok" | "gone">("ok");
const shareSetting = ref<Setting | null>(null);

const tryFetch = async (fn: () => Promise<void>) => {
  try {
    await fn();
  } catch (err) {
    if (err instanceof AxiosError && err.response?.status === 410) {
      status.value = "gone";
    }
    // 403: data type not in scope - leave store empty, component shows no-data state
  }
};

onMounted(async () => {
  const shareApi = getShareApi();
  await Promise.all([
    tryFetch(async () => {
      const res = await shareApi.getShareSalaries(token);
      salaryStore.salaries = res.data;
    }),
    tryFetch(async () => {
      const res = await shareApi.getShareQualifications(token);
      qualificationStore.qualifications = res.data;
    }),
    tryFetch(async () => {
      const res = await shareApi.getShareVocabularies(token);
      vocabularyStore.vocabularies = res.data;
    }),
    tryFetch(async () => {
      const res = await shareApi.getShareSetting(token);
      shareSetting.value = res.data;
    }),
  ]);
});
</script>
