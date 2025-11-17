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
          <span class="font-bold ml-2">Edit</span>
        </Button>
      </div>
    </div>

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
        :is-loading="loadingQueue.length > 0"
        row-clickable
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
import type {
  GetQualificationsRankEnum,
  GetQualificationsStatusEnum,
  Qualification,
  SettingQualification,
} from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import SearchConditionForm from "~/components/qualification/SearchConditionForm.vue";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useQualificationStore } from "~/stores/qualification";
import { getRankColorHexCode } from "~/utils/qualification";
import type { Rank } from "~/utils/qualification";
import { generateRandomString } from "~/utils/rand";

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const qualificationStore = useQualificationStore();
const router = useRoute();
const rank = router.query?.rank as GetQualificationsRankEnum;

const loadingQueue = ref<string[]>([]);
const fetchQualificationApi = async () => {
  const id = generateRandomString();
  loadingQueue.value.push(id);
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
    loadingQueue.value = loadingQueue.value.filter((e) => e !== id);
  }
};

onMounted(async () => {
  await fetchQualificationApi();
});

const selectedStatus = ref<GetQualificationsStatusEnum[]>([]);
const selectedRank = ref<GetQualificationsRankEnum[]>(rank ? [rank] : []);
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

type QualificationWithIndex = Qualification & { index: number };
const columnDefs: ColumnDef<QualificationWithIndex>[] = [
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
