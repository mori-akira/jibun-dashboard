<template>
  <div>
    <Breadcrumb
      :items="[{ text: 'Qualification', iconName: 'tabler:certificate' }]"
    />

    <div
      v-if="status === 'forbidden'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:lock" class="text-5xl" />
      <p>This share link does not include qualification data.</p>
    </div>

    <div
      v-else-if="status === 'gone'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:clock-off" class="text-5xl" />
      <p>This share link has expired.</p>
    </div>

    <template v-else>
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
            :setting-qualification="shareSetting?.qualification"
            wrapper-class="w-4/5"
          />
        </div>
      </Panel>

      <Panel wrapper-class="overflow-x-auto">
        <h3>
          <Icon name="tabler:list" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Result</span>
        </h3>
        <DataTable
          :rows="rows"
          :column-defs="columnDefs"
          :is-loading="isLoading"
          row-clickable
          :init-sort-state="initSortState"
          wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
          header-class="font-cursive h-8 bg-gray-800 text-white"
          @click:row="onClickRow"
        />
      </Panel>

      <ModalWindow
        :show-modal="selectedQualification !== null"
        @close="onCloseModal"
      >
        <InformationForm
          :item="selectedQualification"
          :item-defs="itemDefs"
          wrapper-class="flex flex-col items-center w-[80vw] max-w-3xl"
          label-class="bg-gray-800 text-white w-1/3 font-cursive"
          item-class="bg-gray-200 w-2/3"
        />
      </ModalWindow>
    </template>
  </div>
</template>

<script setup lang="ts">
import { AxiosError } from "axios";
import type { Ref } from "vue";
import type {
  GetQualificationsRankEnum,
  GetQualificationsStatusEnum,
  Qualification,
  Setting,
  SettingQualification,
} from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";
import { useQualificationStore } from "~/stores/qualification";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getRankColorHexCode } from "~/utils/qualification";
import type { Rank } from "~/utils/qualification";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import RankSummary from "~/components/qualification/RankSummary.vue";
import SearchConditionForm from "~/components/qualification/SearchConditionForm.vue";

definePageMeta({ layout: "share" });

const route = useRoute();
const token = route.params.token as string;
const { getShareApi } = useApiClient();
const qualificationStore = useQualificationStore();
const { isLoading, withLoading } = useLoadingQueue();

const status = ref<"ok" | "forbidden" | "gone">("ok");
const allQualifications = ref<Qualification[]>([]);
const shareSetting = ref<Setting | null>(null);
const shareStatus = inject<Ref<"ok" | "gone">>("shareStatus")!;
const forbiddenTypes = inject<Ref<string[]>>("forbiddenTypes")!;

onMounted(async () => {
  await withLoading(async () => {
    try {
      const [qualificationRes, settingRes] = await Promise.all([
        getShareApi().getShareQualifications(token),
        getShareApi().getShareSetting(token),
      ]);
      allQualifications.value = qualificationRes.data;
      shareSetting.value = settingRes.data;
      applyFilters();
    } catch (err) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          status.value = "forbidden";
          forbiddenTypes.value = [...forbiddenTypes.value, "qualification"];
        } else if (err.response?.status === 410) status.value = "gone";
      }
    }
    if (status.value === "gone") shareStatus.value = "gone";
  });
});

const selectedStatus = ref<GetQualificationsStatusEnum[]>([]);
const selectedRank = ref<GetQualificationsRankEnum[]>([]);
const qualificationName = ref("");
const organization = ref("");
const acquiredDateFrom = ref("");
const acquiredDateTo = ref("");
const expirationDateFrom = ref("");
const expirationDateTo = ref("");

const applyFilters = () => {
  qualificationStore.qualifications = allQualifications.value.filter((q) => {
    if (
      selectedStatus.value.length &&
      !selectedStatus.value.includes(q.status as GetQualificationsStatusEnum)
    )
      return false;
    if (
      selectedRank.value.length &&
      !selectedRank.value.includes(q.rank as GetQualificationsRankEnum)
    )
      return false;
    if (
      qualificationName.value &&
      !q.qualificationName
        .toLowerCase()
        .includes(qualificationName.value.toLowerCase())
    )
      return false;
    if (
      organization.value &&
      !q.organization.toLowerCase().includes(organization.value.toLowerCase())
    )
      return false;
    if (
      acquiredDateFrom.value &&
      (!q.acquiredDate || q.acquiredDate < acquiredDateFrom.value)
    )
      return false;
    if (
      acquiredDateTo.value &&
      (!q.acquiredDate || q.acquiredDate > acquiredDateTo.value)
    )
      return false;
    if (
      expirationDateFrom.value &&
      (!q.expirationDate || q.expirationDate < expirationDateFrom.value)
    )
      return false;
    if (
      expirationDateTo.value &&
      (!q.expirationDate || q.expirationDate > expirationDateTo.value)
    )
      return false;
    return true;
  });
};

watch(
  [
    selectedStatus,
    selectedRank,
    qualificationName,
    organization,
    acquiredDateFrom,
    acquiredDateTo,
    expirationDateFrom,
    expirationDateTo,
  ],
  applyFilters,
  { deep: true },
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
    bodyClassFunction: (_value: unknown, row: QualificationWithIndex) =>
      row.status === "expired" ? "text-red-600" : "",
  },
  {
    field: "rank",
    header: "Rank",
    sortable: true,
    headerClass: "w-26",
    bodyClass: "text-center h-12 font-bold",
    bodyStyleFunction: (value: unknown) => ({
      color: getRankColorHexCode(
        value as Rank,
        shareSetting.value?.qualification as SettingQualification,
      ),
    }),
  },
  {
    field: "acquiredDate",
    header: "Acquired",
    sortable: true,
    headerClass: "w-30",
    bodyClass: "text-center h-12",
    bodyClassFunction: (_value: unknown, row: QualificationWithIndex) =>
      row.status === "expired" ? "text-red-600 line-through" : "",
  },
  {
    field: "expirationDate",
    header: "Expiration",
    sortable: true,
    headerClass: "w-30",
    bodyClass: "text-center h-12",
    bodyClassFunction: (_value: unknown, row: QualificationWithIndex) =>
      row.status === "expired" ? "text-red-600 line-through" : "",
  },
];
const rows = computed(() =>
  (qualificationStore.qualifications ?? []).map((e, i) => ({
    ...e,
    index: i + 1,
  })),
);
const initSortState: SortDef<QualificationWithIndex> = {
  column: "index",
  direction: "asc",
};
const onClickRow = (row: QualificationWithIndex) => {
  const filtered = qualificationStore.qualifications?.find(
    (e) => e.qualificationId === row.qualificationId,
  );
  selectedQualification.value = filtered ?? null;
};

const selectedQualification = ref<Qualification | null>(null);
const itemDefs: ItemDef[] = [
  { field: "qualificationId", label: "Id", skipIfNull: true },
  { field: "qualificationName", label: "Qualification Name", skipIfNull: true },
  { field: "abbreviation", label: "Abbreviation", skipIfNull: true },
  { field: "version", label: "Version", skipIfNull: true },
  { field: "status", label: "Status", skipIfNull: true },
  {
    field: "rank",
    label: "Rank",
    skipIfNull: true,
    itemClass: "font-bold",
    itemStyleFunction: (value: unknown) => ({
      color: getRankColorHexCode(
        value as Rank,
        shareSetting.value?.qualification as SettingQualification,
      ),
    }),
  },
  { field: "organization", label: "Organization", skipIfNull: true },
  { field: "acquiredDate", label: "Acquired Date", skipIfNull: true },
  { field: "expirationDate", label: "Expiration Date", skipIfNull: true },
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
