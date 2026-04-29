<template>
  <div>
    <Breadcrumb :items="[{ text: 'Home', iconName: 'tabler:home' }]" />

    <div
      v-if="status === 'gone'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:clock-off" class="text-5xl" />
      <p>This share link has expired.</p>
    </div>

    <template v-else>
      <div class="flex justify-between">
        <Panel wrapper-class="w-full">
          <h3>
            <Icon name="tabler:report-money" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Salary</span>
          </h3>
          <div class="h-36 flex items-center justify-center">
            <div
              v-if="salaryStatus === 'forbidden'"
              class="flex flex-col items-center gap-2 text-gray-500"
            >
              <Icon name="tabler:lock" class="text-4xl" />
              <p class="text-sm text-center">
                This share link does not include salary data.
              </p>
            </div>
            <AnnualComparer
              v-else
              :selector="(salary: Salary) => salary.overview.grossIncome + salary.overview.bonus"
              :value-format="(value: number) => `￥${value.toLocaleString()}`"
              :financial-year-start-month="
                shareSetting?.salary.financialYearStartMonth
              "
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
            <div
              v-if="qualificationStatus === 'forbidden'"
              class="flex flex-col items-center gap-2 text-gray-500"
            >
              <Icon name="tabler:lock" class="text-4xl" />
              <p class="text-sm text-center">
                This share link does not include qualification data.
              </p>
            </div>
            <RankSummary
              v-else
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
          <div class="h-36 flex items-center justify-center">
            <div
              v-if="vocabularyStatus === 'forbidden'"
              class="flex flex-col items-center gap-2 text-gray-500"
            >
              <Icon name="tabler:lock" class="text-4xl" />
              <p class="text-sm text-center">
                This share link does not include vocabulary data.
              </p>
            </div>
            <TagCountSummary
              v-else
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
import type { Ref } from "vue";
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
const salaryStatus = ref<"ok" | "forbidden">("ok");
const qualificationStatus = ref<"ok" | "forbidden">("ok");
const vocabularyStatus = ref<"ok" | "forbidden">("ok");
const shareSetting = ref<Setting | null>(null);
const shareStatus = inject<Ref<"ok" | "gone">>("shareStatus")!;
const forbiddenTypes = inject<Ref<string[]>>("forbiddenTypes")!;

const tryFetch = async (fn: () => Promise<void>, onForbidden?: () => void) => {
  try {
    await fn();
  } catch (err) {
    if (err instanceof AxiosError) {
      if (err.response?.status === 410) status.value = "gone";
      else if (err.response?.status === 403) onForbidden?.();
    }
  }
};

onMounted(async () => {
  const shareApi = getShareApi();
  await Promise.all([
    tryFetch(
      async () => {
        const res = await shareApi.getShareSalaries(token);
        salaryStore.salaries = res.data;
      },
      () => {
        salaryStatus.value = "forbidden";
        forbiddenTypes.value = [...forbiddenTypes.value, "salary"];
      },
    ),
    tryFetch(
      async () => {
        const res = await shareApi.getShareQualifications(token);
        qualificationStore.qualifications = res.data;
      },
      () => {
        qualificationStatus.value = "forbidden";
        forbiddenTypes.value = [...forbiddenTypes.value, "qualification"];
      },
    ),
    tryFetch(
      async () => {
        const res = await shareApi.getShareVocabularies(token);
        vocabularyStore.vocabularies = res.data;
      },
      () => {
        vocabularyStatus.value = "forbidden";
        forbiddenTypes.value = [...forbiddenTypes.value, "vocabulary"];
      },
    ),
    tryFetch(async () => {
      const res = await shareApi.getShareSetting(token);
      shareSetting.value = res.data;
    }),
  ]);
  if (status.value === "gone") shareStatus.value = "gone";
});
</script>
