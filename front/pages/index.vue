<template>
  <div>
    <h2>
      <Icon name="tabler:home" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Home</span>
    </h2>

    <div class="flex justify-between">
      <Panel panel-class="w-5/12">
        <h3>
          <Icon name="tabler:report-money" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Salary</span>
        </h3>
      </Panel>
      <Panel panel-class="w-5/12">
        <h3>
          <Icon name="tabler:certificate" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Qualification</span>
        </h3>
        <div class="flex justify-center">
          <RankSummary
            :qualifications="qualificationStore.qualifications ?? []"
            wrapper-class="w-full"
            can-navigate
          />
        </div>
      </Panel>
    </div>

    <div>
      <Button wrapper-class="mt-4" @click="onAddError"> Add Error </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import { useCommonStore } from "~/stores/common";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import { useQualificationStore } from "~/stores/qualification";

const commonStore = useCommonStore();
const qualificationStore = useQualificationStore();

onMounted(async () => {
  await qualificationStore.fetchQualification();
});

const onAddError = () => {
  commonStore.addErrorMessage("This is a test error message.");
};
</script>
