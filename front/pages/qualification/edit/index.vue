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
          <span class="font-bold ml-2">Delete All</span>
        </Button>
        <div class="flex">
          <Button
            type="action"
            size="small"
            wrapper-class="mr-4"
            @click="onReorderItems"
          >
            <Icon name="tabler:menu-order" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Reorder Items</span>
          </Button>
          <Button
            type="add"
            size="small"
            wrapper-class="mr-8"
            @click="onAddNewOne"
          >
            <Icon name="tabler:plus" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Add New One</span>
          </Button>
        </div>
      </div>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="loadingQueue.length > 0"
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
      />
    </Panel>

    <ModalWindow
      :show-modal="editTargetQualification !== undefined"
      modal-box-class="w-1/2 h-80vh flex-col items-center overflow-y-auto"
      @close="onCloseEditModal"
    >
      <FormEditor
        :target-qualification="editTargetQualification"
        @submit="onSubmitEdit"
        @close-modal="onCloseEditModal"
      />
    </ModalWindow>

    <ModalWindow
      :show-modal="reorderTargetQualification !== undefined"
      modal-box-class="w-1/2 h-80vh flex-col items-center overflow-y-auto"
      @close="onCloseReorderModal"
    >
      <ReorderEditor
        :target-qualifications="reorderTargetQualification"
        @submit="onSubmitReorder"
        @close-modal="onCloseReorderModal"
      />
    </ModalWindow>

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
import { useI18n } from "vue-i18n";

import type {
  GetQualificationRankEnum,
  GetQualificationStatusEnum,
  Qualification,
  SettingQualification,
} from "~/generated/api/client";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import Dialog from "~/components/common/Dialog.vue";
import SearchConditionForm from "~/components/qualification/SearchConditionForm.vue";
import FormEditor from "~/components/qualification/edit/FormEditor.vue";
import ReorderEditor from "~/components/qualification/edit/ReorderEditor.vue";
import {
  useInfoDialog,
  useConfirmDialog,
  useErrorDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useQualificationStore } from "~/stores/qualification";
import { generateRandomString } from "~/utils/rand";
import { getErrorMessage } from "~/utils/error";
import { withErrorHandling } from "~/utils/api-call";

const { t } = useI18n();
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
const { showErrorDialog, errorDialogMessage, openErrorDialog, onErrorOk } =
  useErrorDialog();

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
  {
    header: "Action",
    actionButtons: ["edit"],
    onClickEdit: (row: QualificationWithIndex) => {
      const target = qualificationStore.qualifications?.find(
        (e) => e.qualificationId === row.qualificationId
      );
      editTargetQualification.value = target
        ? {
            qualificationId: target.qualificationId,
            order: target.order,
            qualificationName: target.qualificationName,
            abbreviation: target.abbreviation,
            version: target.version,
            status: target.status,
            rank: target.rank,
            organization: target.organization,
            acquiredDate: target.acquiredDate,
            expirationDate: target.expirationDate,
            officialUrl: target.officialUrl,
            certificationUrl: target.certificationUrl,
            badgeUrl: target.badgeUrl,
          }
        : undefined;
    },
    headerClass: "w-20",
    bodyClass: "h-12",
    iconClass: "w-5 h-5 text-indigo-700 translate-y-1",
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

const editTargetQualification = ref<Qualification | undefined>(undefined);
const onAddNewOne = () =>
  (editTargetQualification.value = {
    qualificationName: "",
    order: (qualificationStore.qualifications?.length ?? 0) + 1,
    status: "dream",
    rank: "D",
    organization: "",
    officialUrl: "",
  });
const onSubmitEdit = async (value: Qualification) => {
  const result = await withErrorHandling(async () => {
    await qualificationStore.putQualification(
      editTargetQualification?.value?.qualificationId,
      {
        order: editTargetQualification.value?.order ?? 1,
        qualificationName: value.qualificationName,
        abbreviation: value.abbreviation || undefined,
        version: value.version || undefined,
        status: value.status,
        rank: value.rank,
        organization: value.organization,
        acquiredDate: value.acquiredDate || undefined,
        expirationDate: value.expirationDate || undefined,
        officialUrl: value.officialUrl,
        certificationUrl: value.certificationUrl || undefined,
        badgeUrl: value.badgeUrl || undefined,
      }
    );
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    editTargetQualification.value = undefined;
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchQualificationApi();
  }
};
const onCloseEditModal = async () => {
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      t("message.confirm.checkUnsavedChanges")
    );
    if (!confirmed) {
      return;
    }
  }
  editTargetQualification.value = undefined;
  commonStore.setHasUnsavedChange(false);
};

const reorderTargetQualification = ref<Qualification[] | undefined>(undefined);
const onReorderItems = () =>
  (reorderTargetQualification.value = (
    qualificationStore.qualifications ?? []
  ).map((e) => ({ ...e })));
const onSubmitReorder = async (targetQualifications: Qualification[]) => {
  const original = new Map(
    (qualificationStore.qualifications ?? []).map((q) => [q.qualificationId, q])
  );
  const changed = targetQualifications.filter(
    (target) => original.get(target.qualificationId)?.order !== target.order
  );
  const result = await withErrorHandling(async () => {
    await Promise.all(
      changed.map(async (e) => {
        const target = original.get(e.qualificationId);
        if (!target) {
          return;
        }
        await qualificationStore.putQualification(e.qualificationId, {
          ...target,
          order: e.order,
        });
      })
    );
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    reorderTargetQualification.value = undefined;
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchQualificationApi();
  }
};
const onCloseReorderModal = async () => {
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      t("message.confirm.checkUnsavedChanges")
    );
    if (!confirmed) {
      return;
    }
  }
  reorderTargetQualification.value = undefined;
  commonStore.setHasUnsavedChange(false);
};

const onDeleteAll = async () => {
  const confirmed = await openConfirmDialog(
    t("message.confirm.deleteAllQualifications")
  );
  if (!confirmed) {
    return;
  }
  const id = commonStore.addLoadingQueue();
  try {
    await Promise.all(
      checkedId.value.map(async (e) =>
        qualificationStore.deleteQualification(e)
      )
    );
    checkedId.value = [];
    commonStore.deleteLoadingQueue(id);
    await openInfoDialog(t("message.info.completeSuccessfully"));
  } catch (err) {
    console.error(err);
    await openErrorDialog(t("message.error.somethingCloudNotDeleted"));
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
  await fetchQualificationApi();
};
</script>
