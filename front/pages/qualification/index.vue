<template>
  <div>
    <h2>
      <Icon name="tabler:certificate" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Qualification</span>
    </h2>

    <Panel>
      <h3>
        <Icon name="tabler:search" class="adjust-icon" />
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

    <Panel panel-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:list" class="adjust-icon" />
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
        wrapper-class="flex flex-col items-center w-80vw"
        label-class="bg-gray-800 text-white w-1/3 font-cursive"
        item-class="bg-gray-200 w-2/3"
      />
    </ModalWindow>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import type {
  Qualification,
  GetQualificationStatusEnum,
  GetQualificationRankEnum,
} from "~/api/client/api";
import Panel from "~/components/common/Panel.vue";
import Accordion from "~/components/common/Accordion.vue";
import MultiOptionSelector from "~/components/common/MultiOptionSelector.vue";
import TextBox from "~/components/common/TextBox.vue";
import DatePickerFromTo from "~/components/common/DatePickerFromTo.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import { useQualificationStore } from "~/stores/qualification";

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

const selectedRank = ref<GetQualificationRankEnum[]>([]);
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
  qualificationName.value = value;
  await fetchQualificationApi();
};

const organization = ref<string>("");
const onBlurOrganization = async (value: string) => {
  organization.value = value;
  await fetchQualificationApi();
};

const acquiredDateFrom = ref<string>("");
const onChangeAcquiredDateFrom = async (value: string) => {
  acquiredDateFrom.value = value;
  await fetchQualificationApi();
};

const acquiredDateTo = ref<string>("");
const onChangeAcquiredDateTo = async (value: string) => {
  acquiredDateTo.value = value;
  await fetchQualificationApi();
};

const expirationDateFrom = ref<string>("");
const onChangeExpirationDateFrom = async (value: string) => {
  acquiredDateFrom.value = value;
  await fetchQualificationApi();
};

const expirationDateTo = ref<string>("");
const onChangeExpirationDateTo = async (value: string) => {
  acquiredDateTo.value = value;
  await fetchQualificationApi();
};

const fetchQualificationApi = async () => {
  isLoading.value = true;
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
  isLoading.value = false;
};

const getRankColorClass = (rank: unknown): string => {
  switch (rank) {
    case "A":
      return "text-blue-500";
    case "B":
      return "text-green-500";
    case "C":
      return "text-orange-500";
    case "D":
      return "text-yellow-500";
    default:
      return "";
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
    bodyClassFunction: (value) => getRankColorClass(value),
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
const rows = ref<Qualification[]>(
  qualificationStore.qualifications?.map((e, i) => ({ ...e, index: i + 1 })) ??
    []
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
    itemClassFunction: (value) => getRankColorClass(value),
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
