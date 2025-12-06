<template>
  <div>
    <Breadcrumb
      :items="[{ text: 'Qualification', iconName: 'tabler:certificate' }]"
    />

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:search" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Condition</span>
        </h3>
        <SearchConditionForm
          v-model:selected-status="selectedStatus"
          v-model:selected-rank="selectedRank"
          hide-accordion
          status-wrapper-class="flex-wrap"
          status-label-class="!w-50"
          status-options-class="mt-2 !ml-0 mr-2"
        />
      </Panel>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2">
        <h3>
          <Icon name="tabler:chart-infographic" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Summary</span>
        </h3>
        <div class="flex justify-center">
          <RankSummary
            :qualifications="qualificationStore.qualifications ?? []"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel panel-class="w-full ml-2 overflow-x-auto">
        <h3>
          <Icon name="tabler:list" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Result</span>
        </h3>
        <DataTable
          :rows="rows"
          :column-defs="columnDefs"
          :is-loading="loadingQueue.length > 0"
          row-clickable
          :init-sort-state="initSortState"
          wrapper-class="flex justify-center mt-4 mx-1"
          header-class="font-cursive h-8 bg-gray-800 text-white"
          @click:row="onClickRow"
        ></DataTable>
      </Panel>
    </div>

    <ModalWindow
      :show-modal="selectedQualification !== null"
      modal-box-class="overflow-x-auto"
      @close="onCloseModal"
    >
      <InformationForm
        :item="selectedQualification"
        :item-defs="itemDefs"
        wrapper-class="flex flex-col items-center w-116"
        label-class="bg-gray-800 text-white w-45 font-cursive"
        item-class="bg-gray-200 w-70"
      />
    </ModalWindow>
  </div>
</template>

<script setup lang="ts">
import type {
  GetQualificationsRankEnum,
  GetQualificationsStatusEnum,
  Qualification,
  SettingQualification,
} from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import SearchConditionForm from "~/components/qualification/SearchConditionForm.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useQualificationStore } from "~/stores/qualification";

definePageMeta({
  layout: "mobile",
});

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const qualificationStore = useQualificationStore();

const loadingQueue = ref<string[]>([]);
const fetchQualificationApi = async () => {
  const id = generateRandomString();
  loadingQueue.value.push(id);
  try {
    await qualificationStore.fetchQualification(
      undefined,
      selectedStatus.value,
      selectedRank.value
    );
  } catch (err) {
    console.error(err);
    commonStore.addErrorMessage(getErrorMessage(err));
  } finally {
    loadingQueue.value = loadingQueue.value.filter((e) => e !== id);
  }
};

const selectedStatus = ref<GetQualificationsStatusEnum[]>([]);
const selectedRank = ref<GetQualificationsRankEnum[]>([]);
watch(
  () => [selectedStatus, selectedRank],
  async () => await fetchQualificationApi(),
  { immediate: true, deep: true }
);

type QualificationWithIndex = Qualification & { index: number };
const columnDefs: ColumnDef<QualificationWithIndex>[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12 font-s text-[0.9rem]",
  },
  {
    field: "qualificationName",
    header: "Name",
    sortable: true,
    headerClass: "w-60",
    bodyClass: "h-12 text-[0.9rem]",
  },
  {
    field: "status",
    header: "Status",
    sortable: true,
    headerClass: "w-26",
    bodyClass: "text-center h-12 text-[0.9rem]",
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
    bodyClass: "text-center h-12 text-[0.9rem]",
  },
  {
    field: "expirationDate",
    header: "Expiration",
    sortable: true,
    headerClass: "w-30",
    bodyClass: "text-center h-12 text-[0.9rem]",
  },
];
const rows = computed(() =>
  (qualificationStore.qualifications ?? []).map((e, i) => ({
    ...e,
    index: i + 1,
  }))
);
const initSortState: SortDef<QualificationWithIndex> = {
  column: "index",
  direction: "asc",
};
const onClickRow = (row: QualificationWithIndex) => {
  const filtered = qualificationStore.qualifications?.find(
    (e) => e.qualificationId === row.qualificationId
  );
  selectedQualification.value = filtered ?? null;
};

const selectedQualification = ref<Qualification | null>(null);
const itemDefs: ItemDef[] = [
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
