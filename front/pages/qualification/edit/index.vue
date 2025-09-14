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
        :is-loading="loadingQueue.length > 0"
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
      />
    </Panel>

    <ModalWindow
      :show-modal="editTargetQualification !== undefined"
      modal-box-class="w-1/2 h-80vh flex-col items-center overflow-y-auto"
      @close="onCloseModal"
    >
      <Form v-slot="{ meta, handleSubmit }">
        <div class="flex justify-end">
          <IconButton
            type="cancel"
            icon-class="w-6 h-6"
            @click="onCloseModal"
          />
        </div>
        <template v-for="def in editFieldDefs" :key="`filed-${def.key}`">
          <Field
            v-slot="{ field, errorMessage }"
            :name="def.key"
            :rules="validationRules[def.key]"
            :value="editTargetQualification?.[def.key] ?? ''"
          >
            <TextBox
              v-if="def.type === 'textbox'"
              :label="def.label"
              v-bind="field"
              :error-message="errorMessage"
              :required="def.required"
              wrapper-class="mt-4 w-full justify-center"
              label-class="w-40 ml-4 font-cursive"
              input-wrapper-class="w-1/2"
              @blur:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
            <SelectBox
              v-if="def.type === 'select'"
              :label="def.label"
              :value="field.value"
              :options="
                def?.options?.map((e) => ({ label: e, value: e })) ?? []
              "
              :required="def.required"
              wrapper-class="mt-4 w-full justify-center"
              label-class="w-40 ml-4 font-cursive"
              select-wrapper-class="w-1/2"
              select-class="text-center"
              @change:value="
                (value) => {
                  commonStore.setHasUnsavedChange(true);
                  field?.['onUpdate:modelValue']?.(value);
                }
              "
            />
            <DatePicker
              v-if="def.type === 'datepicker'"
              :label="def.label"
              :date="field.value"
              :required="def.required"
              wrapper-class="mt-4 w-full justify-center"
              label-class="w-40 ml-4 font-cursive"
              pickers-wrapper-class="w-1/2 px-12"
              @change="
                (value) => {
                  commonStore.setHasUnsavedChange(true);
                  field?.['onUpdate:modelValue']?.(value);
                }
              "
            />
          </Field>
        </template>
        <div class="m-4">
          <Button
            :disabled="!meta.valid"
            type="action"
            wrapper-class="flex justify-center mt-8"
            @click="handleSubmit(onSubmit)"
          >
            <Icon name="tabler:database-share" class="adjust-icon-4" />
            <span class="ml-2">Execute</span>
          </Button>
        </div>
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
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";
import { useI18n } from "vue-i18n";

import type {
  GetQualificationRankEnum,
  GetQualificationStatusEnum,
  Qualification,
  SettingQualification,
} from "~/api/client";
import { schemas } from "~/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import DatePicker from "~/components/common/DatePicker.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
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
import { generateRandomString } from "~/utils/rand";

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
  type: "textbox" | "select" | "datepicker";
  options?: string[];
  required: boolean;
}[] = [
  {
    key: "qualificationName",
    label: "Qualification Name",
    type: "textbox",
    required: true,
  },
  {
    key: "abbreviation",
    label: "Abbreviation",
    type: "textbox",
    required: false,
  },
  { key: "version", label: "Version", type: "textbox", required: false },
  {
    key: "status",
    label: "Status",
    type: "select",
    options: ["dream", "planning", "acquired"],
    required: true,
  },
  {
    key: "rank",
    label: "Rank",
    type: "select",
    options: ["A", "B", "C", "D"],
    required: true,
  },
  {
    key: "organization",
    label: "Organization",
    type: "textbox",
    required: true,
  },
  {
    key: "acquiredDate",
    label: "Acquired Date",
    type: "datepicker",
    required: false,
  },
  {
    key: "expirationDate",
    label: "Expiration Date",
    type: "datepicker",
    required: false,
  },
  {
    key: "officialUrl",
    label: "Official Url",
    type: "textbox",
    required: true,
  },
  {
    key: "certificationUrl",
    label: "Certification Url",
    type: "textbox",
    required: false,
  },
  { key: "badgeUrl", label: "Badge Url", type: "textbox", required: false },
];

const onAddNewOne = () =>
  (editTargetQualification.value = {
    qualificationName: "",
    status: "dream",
    rank: "D",
    organization: "",
    officialUrl: "",
  });
const onSubmit: SubmissionHandler<GenericObject> = async (value) => {
  const valueTyped = value as Qualification;
  const result = await withErrorHandling(async () => {
    qualificationStore.putQualification({
      qualificationId: editTargetQualification?.value?.qualificationId,
      qualificationName: valueTyped.qualificationName,
      abbreviation: valueTyped.abbreviation || undefined,
      version: valueTyped.version || undefined,
      status: valueTyped.status,
      rank: valueTyped.rank,
      organization: valueTyped.organization,
      acquiredDate: valueTyped.acquiredDate || undefined,
      expirationDate: valueTyped.expirationDate || undefined,
      officialUrl: valueTyped.officialUrl,
      certificationUrl: valueTyped.certificationUrl || undefined,
      badgeUrl: valueTyped.badgeUrl || undefined,
    });
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    editTargetQualification.value = undefined;
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchQualificationApi();
  }
};
const onCloseModal = async () => {
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
    commonStore.deleteLoadingQueue(id);
    checkedId.value = [];
    await openInfoDialog(t("message.info.completeSuccessfully"));
  } catch (err) {
    console.error(err);
    commonStore.deleteLoadingQueue(id);
    await openErrorDialog(t("message.error.somethingCloudNotDeleted"));
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
  await fetchQualificationApi();
};
</script>
