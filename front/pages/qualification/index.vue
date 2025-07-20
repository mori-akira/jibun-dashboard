<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[{ text: 'Qualification', iconName: 'tabler:certificate' }]"
      />
      <div class="flex items-center mr-4">
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-32"
          @click="() => navigateTo('/qualification/edit')"
        >
          <Icon name="tabler:database-edit" class="text-base translate-y-0.5" />
          <span class="font-cursive font-bold ml-2">Edit</span>
        </Button>
      </div>
    </div>

    <Panel>
      <h3>
        <Icon name="tabler:search" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Condition</span>
      </h3>
      <MultiOptionSelector
        label="Status"
        :options="['acquired', 'planning', 'dream']"
        :values="['acquired', 'planning', 'dream']"
        :selected-options="selectedStatus"
        wrapper-class="m-4 flex justify-start items-center"
        label-class="w-25 font-cursive"
        @click:value="onClickStatusOption"
      ></MultiOptionSelector>
      <MultiOptionSelector
        label="Rank"
        :options="['A', 'B', 'C', 'D']"
        :values="['A', 'B', 'C', 'D']"
        :selected-options="selectedRank"
        wrapper-class="m-4 flex justify-start items-center"
        label-class="w-25 font-cursive"
        @click:value="onClickRankOption"
      ></MultiOptionSelector>

      <Accordion title="More Detail" title-class="font-cursive">
        <TextBox
          label="Qualification Name"
          :value="qualificationName"
          wrapper-class="m-4"
          label-class="w-40 font-cursive"
          input-wrapper-class="w-1/2"
          @blur:value="onBlurQualificationName"
        />
        <TextBox
          label="Organization"
          :value="organization"
          wrapper-class="m-4"
          label-class="w-40 font-cursive"
          input-wrapper-class="w-1/2"
          @blur:value="onBlurOrganization"
        />
        <DatePickerFromTo
          label="Acquired Date"
          :date-from="acquiredDateFrom"
          :date-to="acquiredDateTo"
          wrapper-class="m-4"
          label-class="w-40 font-cursive"
          pickers-wrapper-class="min-w-96 w-1/2"
          @change:from="onChangeAcquiredDateFrom"
          @change:to="onChangeAcquiredDateTo"
        />
        <DatePickerFromTo
          label="Expiration Date"
          :date-from="expirationDateFrom"
          :date-to="expirationDateTo"
          wrapper-class="m-4"
          label-class="w-40 font-cursive"
          pickers-wrapper-class="min-w-96 w-1/2"
          @change:from="onChangeExpirationDateFrom"
          @change:to="onChangeExpirationDateTo"
        />
      </Accordion>
    </Panel>

    <Panel>
      <h3>
        <Icon name="tabler:chart-infographic" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Summary</span>
      </h3>
      <div class="flex justify-center">
        <RankSummary
          :qualifications="qualificationStore.qualifications ?? []"
          wrapper-class="w-4/5"
        />
      </div>
    </Panel>

    <Panel panel-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:list" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Result</span>
      </h3>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        row-clickable
        row-action-key="qualificationId"
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
        @click:row="onClickRow"
      ></DataTable>
    </Panel>

    <ModalWindow
      :show-modal="selectedQualification !== null"
      @close="onCloseModal"
    >
      <InformationForm
        :item="selectedQualification"
        :item-defs="itemDefs"
        wrapper-class="flex flex-col items-center w-80vw max-w-3xl"
        label-class="bg-gray-800 text-white w-1/3 font-cursive"
        item-class="bg-gray-200 w-2/3"
      />
    </ModalWindow>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRoute } from "vue-router";

import type {
  Qualification,
  GetQualificationStatusEnum,
  GetQualificationRankEnum,
  SettingQualification,
} from "~/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Accordion from "~/components/common/Accordion.vue";
import MultiOptionSelector from "~/components/common/MultiOptionSelector.vue";
import TextBox from "~/components/common/TextBox.vue";
import DatePickerFromTo from "~/components/common/DatePickerFromTo.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useQualificationStore } from "~/stores/qualification";
import { getRankColorHexCode } from "~/utils/qualification";
import type { Rank } from "~/utils/qualification";

const router = useRoute();
const rank = router.query?.rank;
const commonStore = useCommonStore();
const settingStore = useSettingStore();
const qualificationStore = useQualificationStore();

onMounted(async () => {
  await fetchQualificationApi();
});

const selectedStatus = ref<GetQualificationStatusEnum[]>([]);
const onClickStatusOption = async (value: string) => {
  if (selectedStatus.value.includes(value as GetQualificationStatusEnum)) {
    selectedStatus.value = selectedStatus.value.filter((e) => e !== value);
  } else {
    selectedStatus.value.push(value as GetQualificationStatusEnum);
  }
  await fetchQualificationApi();
};

const selectedRank = ref<GetQualificationRankEnum[]>(
  rank ? [rank as GetQualificationRankEnum] : []
);
const onClickRankOption = async (value: string) => {
  if (selectedRank.value.includes(value as GetQualificationRankEnum)) {
    selectedRank.value = selectedRank.value.filter((e) => e !== value);
  } else {
    selectedRank.value.push(value as GetQualificationRankEnum);
  }
  await fetchQualificationApi();
};

const qualificationName = ref<string>("");
const onBlurQualificationName = async (value: string) => {
  if (qualificationName.value === value) {
    return;
  }
  qualificationName.value = value;
  await fetchQualificationApi();
};

const organization = ref<string>("");
const onBlurOrganization = async (value: string) => {
  if (organization.value === value) {
    return;
  }
  organization.value = value;
  await fetchQualificationApi();
};

const acquiredDateFrom = ref<string | undefined>("");
const onChangeAcquiredDateFrom = async (value: string | undefined) => {
  if (acquiredDateFrom.value === value) {
    return;
  }
  acquiredDateFrom.value = value;
  await fetchQualificationApi();
};

const acquiredDateTo = ref<string | undefined>("");
const onChangeAcquiredDateTo = async (value: string | undefined) => {
  if (acquiredDateTo.value === value) {
    return;
  }
  acquiredDateTo.value = value;
  await fetchQualificationApi();
};

const expirationDateFrom = ref<string | undefined>("");
const onChangeExpirationDateFrom = async (value: string | undefined) => {
  if (expirationDateFrom.value === value) {
    return;
  }
  expirationDateFrom.value = value;
  await fetchQualificationApi();
};

const expirationDateTo = ref<string | undefined>("");
const onChangeExpirationDateTo = async (value: string | undefined) => {
  if (expirationDateTo.value === value) {
    return;
  }
  expirationDateTo.value = value;
  await fetchQualificationApi();
};

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

const columnDefs: ColumnDef[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12",
  },
  {
    field: "qualificationName",
    header: "Name",
    sortable: true,
    headerClass: "w-96",
    bodyClass: "h-12",
  },
  {
    field: "status",
    header: "Status",
    sortable: true,
    headerClass: "w-26",
    bodyClass: "text-center h-12",
  },
  {
    field: "rank",
    header: "Rank",
    sortable: true,
    headerClass: "w-26",
    bodyClass: "text-center h-12 font-bold",
    bodyStyleFunction: (value) => ({
      color: getRankColorHexCode(
        value as Rank,
        settingStore.setting?.qualification as SettingQualification
      ),
    }),
  },
  {
    field: "acquiredDate",
    header: "Acquired",
    sortable: true,
    headerClass: "w-30",
    bodyClass: "text-center h-12",
  },
  {
    field: "expirationDate",
    header: "Expiration",
    sortable: true,
    headerClass: "w-30",
    bodyClass: "text-center h-12",
  },
];
const rows = computed(() =>
  (qualificationStore.qualifications ?? []).map((e, i) => ({
    ...e,
    index: i + 1,
  }))
);
const isLoading = ref<boolean>(false);
const initSortState: SortDef = {
  column: "index",
  direction: "asc",
};
const onClickRow = (id: unknown) => {
  const filtered = qualificationStore.qualifications?.filter(
    (e) => e.qualificationId === id
  );
  selectedQualification.value = filtered ? filtered[0] : null;
};

const selectedQualification = ref<Qualification | null>(null);
const itemDefs: ItemDef[] = [
  {
    field: "qualificationId",
    label: "Id",
    skipIfNull: true,
  },
  {
    field: "qualificationName",
    label: "Qualification Name",
    skipIfNull: true,
  },
  {
    field: "abbreviation",
    label: "Abbreviation",
    skipIfNull: true,
  },
  {
    field: "version",
    label: "Version",
    skipIfNull: true,
  },
  {
    field: "status",
    label: "Status",
    skipIfNull: true,
  },
  {
    field: "rank",
    label: "Rank",
    skipIfNull: true,
    itemClass: "font-bold",
    itemStyleFunction: (value) => ({
      color: getRankColorHexCode(
        value as Rank,
        settingStore.setting?.qualification as SettingQualification
      ),
    }),
  },
  {
    field: "organization",
    label: "Organization",
    skipIfNull: true,
  },
  {
    field: "acquiredDate",
    label: "Acquired Date",
    skipIfNull: true,
  },
  {
    field: "expirationDate",
    label: "Expiration Date",
    skipIfNull: true,
  },
  {
    field: "officialUrl",
    label: "Official URL",
    skipIfNull: true,
    itemType: "anchorLink",
  },
  {
    field: "certificationUrl",
    label: "Certification URL",
    skipIfNull: true,
    itemType: "anchorLink",
  },
  {
    field: "badgeUrl",
    label: "Badge URL",
    skipIfNull: true,
    itemType: "anchorLink",
  },
];
const onCloseModal = () => (selectedQualification.value = null);
</script>
