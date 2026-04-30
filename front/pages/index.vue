<template>
  <div>
    <Breadcrumb :items="[{ text: 'Home', iconName: 'tabler:home' }]" />

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
            :filter="(q) => q.status === 'acquired'"
            wrapper-class="w-full"
            can-navigate
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
            :on-click-tag="
              (tagId) =>
                navigateTo({ path: '/vocabulary', query: { tagIds: tagId } })
            "
          />
        </div>
      </Panel>
      <Panel wrapper-class="w-full">
        <h3>
          <Icon name="tabler:link" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Shared Links</span>
        </h3>
        <div class="h-36 flex items-center">
          <StatusSummary
            :shared-links="sharedLinkStore.sharedLinks ?? []"
            wrapper-class="w-full"
            :on-navigate="() => navigateTo('/profile/shared-links')"
          />
        </div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import type { Salary } from "~/generated/api/client";
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import { useQualificationStore } from "~/stores/qualification";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useSharedLinkStore } from "~/stores/sharedLink";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import TagCountSummary from "~/components/vocabulary/TagCountSummary.vue";
import StatusSummary from "~/components/shared-link/StatusSummary.vue";
import { withErrorHandling } from "~/utils/api-call";

const commonStore = useCommonStore();
const userStore = useUserStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();
const qualificationStore = useQualificationStore();
const vocabularyStore = useVocabularyStore();
const sharedLinkStore = useSharedLinkStore();

onMounted(async () => {
  await withErrorHandling(async () => {
    await Promise.all([
      userStore.fetchUser(),
      settingStore.fetchSetting(),
      qualificationStore.fetchQualification(),
      salaryStore.fetchSalary(),
      vocabularyStore.fetchVocabularies(),
      sharedLinkStore.fetchSharedLinks(),
    ]);
  }, commonStore);
});
</script>
