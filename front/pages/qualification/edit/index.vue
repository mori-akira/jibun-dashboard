<template>
  <div>
    <Breadcrumb
      :items="[
        {
          text: 'Qualification',
          iconName: 'tabler:certificate',
          link: '/qualification',
        },
        { text: 'Edit', iconName: 'tabler:database-edit' },
      ]"
    />

    <Panel>
      <h3>
        <Icon name="tabler:search" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Condition</span>
      </h3>
      <SearchConditionForm
        v-model:selected-status="selectedStatus"
        v-model:selected-rank="selectedRank"
        v-model:qualification-name="qualificationName"
        v-model:organization="organization"
        v-model:acquired-date-from="acquiredDateFrom"
        v-model:acquired-date-to="acquiredDateTo"
        v-model:expiration-date-from="expirationDateFrom"
        v-model:expiration-date-to="expirationDateTo"
      />
    </Panel>
  </div>
</template>

<script setup lang="ts">
import type {
  GetQualificationRankEnum,
  GetQualificationStatusEnum,
} from "~/api/client";
import Panel from "~/components/common/Panel.vue";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import SearchConditionForm from "~/components/qualification/SearchConditionForm.vue";
import { useCommonStore } from "~/stores/common";
import { useQualificationStore } from "~/stores/qualification";

onMounted(async () => {
  await fetchQualificationApi();
});

const commonStore = useCommonStore();
const qualificationStore = useQualificationStore();

const selectedStatus = ref<GetQualificationStatusEnum[]>([]);
const selectedRank = ref<GetQualificationRankEnum[]>([]);
const qualificationName = ref<string>("");
const organization = ref<string>("");
const acquiredDateFrom = ref<string>("");
const acquiredDateTo = ref<string>("");
const expirationDateFrom = ref<string>("");
const expirationDateTo = ref<string>("");
watch(
  () => [
    selectedStatus,
    selectedRank,
    qualificationName,
    organization,
    acquiredDateFrom,
    acquiredDateTo,
    expirationDateFrom,
    expirationDateTo,
  ],
  async () => await fetchQualificationApi(),
  { immediate: true, deep: true }
);

const fetchQualificationApi = async () => {
  isLoading.value = true;
  try {
    await qualificationStore.fetchQualification(
      qualificationName.value,
      selectedStatus.value,
      selectedRank.value,
      organization.value,
      acquiredDateFrom.value,
      acquiredDateTo.value,
      expirationDateFrom.value,
      expirationDateTo.value
    );
  } catch (err) {
    console.error(err);
    commonStore.addErrorMessage(getErrorMessage(err));
  } finally {
    isLoading.value = false;
  }
};

const isLoading = ref<boolean>(false);
</script>
