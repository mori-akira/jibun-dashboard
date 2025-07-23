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
        <Button
          type="add"
          size="small"
          wrapper-class="mx-8"
          @click="onAddNewOne"
        >
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Add New One</span>
        </Button>
      </div>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
      />
    </Panel>

    <ModalWindow
      :show-modal="editTargetQualification !== undefined"
      modal-box-class="w-2/3 flex-col items-center"
      @close="onCloseModal"
    >
      <Form
        v-slot="{
          /* meta, handleSubmit */
        }"
      >
        <template v-for="def in editFieldDefs" :key="`filed-${def.key}`">
          <Field
            v-slot="{ field, errorMessage }"
            :name="def.key"
            :rules="validationRules[def.key]"
          >
            <TextBox
              :label="def.label"
              v-bind="field"
              :error-message="errorMessage"
              required
              wrapper-class="mt-4 w-full justify-center"
              label-class="w-50 ml-4 font-cursive"
              input-wrapper-class="w-1/3"
              @blur:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
        </template>
      </Form>
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
// import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import type {
  GetQualificationRankEnum,
  GetQualificationStatusEnum,
  Qualification,
  SettingQualification,
} from "~/api/client";
import { schemas } from "~/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
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
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

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

const editTargetQualification = ref<Qualification | undefined>(undefined);
const validationRules: {
  [K in keyof Qualification]?: GenericValidateFunction[];
} = {
  qualificationName: zodToVeeRules(
    schemas.Qualification.shape.qualificationName
  ),
  abbreviation: zodToVeeRules(schemas.Qualification.shape.abbreviation),
  version: zodToVeeRules(schemas.Qualification.shape.version),
  status: zodToVeeRules(schemas.Qualification.shape.status),
  rank: zodToVeeRules(schemas.Qualification.shape.rank),
  organization: zodToVeeRules(schemas.Qualification.shape.organization),
  acquiredDate: zodToVeeRules(schemas.Qualification.shape.acquiredDate),
  expirationDate: zodToVeeRules(schemas.Qualification.shape.expirationDate),
  officialUrl: zodToVeeRules(schemas.Qualification.shape.officialUrl),
  certificationUrl: zodToVeeRules(schemas.Qualification.shape.certificationUrl),
  badgeUrl: zodToVeeRules(schemas.Qualification.shape.badgeUrl),
};
const editFieldDefs: {
  key: keyof Qualification;
  label: string;
}[] = [
  { key: "qualificationName", label: "Qualification Name" },
  { key: "abbreviation", label: "Abbreviation" },
  { key: "version", label: "Version" },
  { key: "status", label: "Status" },
  { key: "rank", label: "Rank" },
  { key: "organization", label: "Organization" },
  { key: "acquiredDate", label: "Acquired Date" },
  { key: "expirationDate", label: "Expiration Date" },
  { key: "officialUrl", label: "Official Url" },
  { key: "certificationUrl", label: "Certification Url" },
  { key: "badgeUrl", label: "Badge Url" },
];
const onAddNewOne = () =>
  (editTargetQualification.value = {
    qualificationName: "",
    abbreviation: "",
    version: "",
    status: "dream",
    rank: "D",
    organization: "",
    acquiredDate: "",
    expirationDate: "",
    officialUrl: "",
    certificationUrl: "",
    badgeUrl: "",
  });
const onCloseModal = () => (editTargetQualification.value = undefined);

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
            userId: target.userId,
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
      console.log(editTargetQualification.value);
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

const onDeleteAll = async () => {
  const confirmed = await openConfirmDialog(
    "Confirm Deletion Of The All Checked Qualification?"
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
    commonStore.deleteLoadingQueue(id);
    checkedId.value = [];
    await openInfoDialog("Process Completed Successfully");
    await fetchQualificationApi();
  } catch (err) {
    console.error(err);
    await openErrorDialog("Something Cloud Not Be Deleted, Try Again.");
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
};
</script>
