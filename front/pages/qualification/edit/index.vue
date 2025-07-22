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

    <Panel panel-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:list" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Result</span>
      </h3>
      <div class="flex justify-between items-center mt-4">
        <Button
          type="delete"
          size="small"
          wrapper-class="mx-8"
          :disabled="checkedId.length === 0"
          @click="onDeleteAll"
        >
          <Icon name="tabler:trash" class="text-base translate-y-0.5" />
          <span class="font-cursive font-bold ml-2">Delete All</span>
        </Button>
        <Button type="add" size="small" wrapper-class="mx-8">
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="font-cursive font-bold ml-2">Add New One</span>
        </Button>
      </div>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        row-clickable
        row-action-key="qualificationId"
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
      ></DataTable>
    </Panel>

    <Dialog
      :show-dialog="showInfoDialog"
      type="info"
      :message="infoDialogMessage"
      button-type="ok"
      @click:ok="onInfoOk"
      @close="onInfoOk"
    />

    <Dialog
      :show-dialog="showConfirmDialog"
      type="confirm"
      :message="confirmDialogMessage"
      button-type="yesNo"
      @click:yes="onConfirmYes"
      @click:no="onConfirmNo"
      @close="onConfirmNo"
    />

    <Dialog
      :show-dialog="showErrorDialog"
      type="error"
      :message="errorDialogMessage"
      button-type="ok"
      @click:ok="onErrorOk"
      @close="onErrorOk"
    />
  </div>
</template>

<script setup lang="ts">
import type {
  GetQualificationRankEnum,
  GetQualificationStatusEnum,
  Qualification,
  SettingQualification,
} from "~/api/client";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import Dialog from "~/components/common/Dialog.vue";
import SearchConditionForm from "~/components/qualification/SearchConditionForm.vue";
import {
  useInfoDialog,
  useConfirmDialog,
  useErrorDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useQualificationStore } from "~/stores/qualification";
import { withErrorHandling } from "~/utils/api-call";

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const qualificationStore = useQualificationStore();
const { showInfoDialog, infoDialogMessage, openInfoDialog, onInfoOk } =
  useInfoDialog();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const { showErrorDialog, errorDialogMessage, openErrorDialog, onErrorOk } =
  useErrorDialog();

const isLoading = ref<boolean>(false);
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

onMounted(async () => {
  await fetchQualificationApi();
});

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

type QualificationWithIndex = Qualification & { index: number };
const checkedId = ref<string[]>([]);
const columnDefs: ColumnDef<QualificationWithIndex>[] = [
  {
    checkable: true,
    checkStatusFunction: (row: QualificationWithIndex) =>
      checkedId.value.includes(row["qualificationId"] as string) ? "on" : "off",
    headerCheckable: true,
    headerCheckStatusFunction: (rows: QualificationWithIndex[]) =>
      checkedId.value.length === 0
        ? "off"
        : checkedId.value.length < rows.length
        ? "neutral"
        : "on",
    headerClass: "w-8",
    bodyClass: "text-center h-12",
    onChangeCheck: (_, row: QualificationWithIndex) => {
      if (!row["qualificationId"]) {
        return;
      }
      if (checkedId.value.includes(row["qualificationId"])) {
        checkedId.value = checkedId.value.filter(
          (e) => e !== row["qualificationId"]
        );
      } else {
        checkedId.value.push(row["qualificationId"]);
      }
    },
    onChangeHeaderCheck: (_, rows: QualificationWithIndex[]) => {
      if (checkedId.value.length < rows.length) {
        rows
          .map((e) => e.qualificationId)
          .filter((e) => e !== undefined)
          .forEach(
            (e) => !checkedId.value.includes(e) && checkedId.value.push(e)
          );
      } else {
        checkedId.value = [];
      }
    },
  },
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
    bodyStyleFunction: (value: unknown) => ({
      color: getRankColorHexCode(
        value as Rank,
        settingStore.setting?.qualification as SettingQualification
      ),
    }),
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

const onDeleteAll = async () => {
  const confirmed = await openConfirmDialog(
    "Confirm Deletion Of The All Checked Qualification?"
  );
  if (!confirmed) {
    return;
  }
  const result = await withErrorHandling(async () => {
    await Promise.all(
      checkedId.value.map(async (e) =>
        qualificationStore.deleteQualification(e)
      )
    );
  }, commonStore);
  if (result) {
    checkedId.value = [];
    await openInfoDialog("Process Completed Successfully");
    await fetchQualificationApi();
  }
};
</script>
