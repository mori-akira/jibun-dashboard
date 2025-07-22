<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Salary', iconName: 'tabler:report-money', link: '/salary' },
        { text: 'Edit', iconName: 'tabler:database-edit' },
      ]"
    />

    <div class="flex justify-center">
      <Panel panel-class="w-2/3">
        <MonthPicker
          label="Target"
          month-picker
          :date="tempDate"
          label-class="w-20 font-cursive"
          pickers-wrapper-class="min-w-40 w-1/5"
          @change="onChangeDate"
        />
      </Panel>
    </div>

    <div class="flex justify-center">
      <Panel panel-class="w-2/3">
        <Tabs
          :tabs="[
            { label: 'Edit As Form', slot: 'editAsForm' },
            { label: 'Edit As JSON', slot: 'editAsJson' },
            { label: 'Upload And OCR', slot: 'uploadAndOcr' },
          ]"
          init-tab="editAsForm"
          button-class="font-cursive"
        >
          <template #editAsForm>
            <FormEditor
              v-model:target-salary="targetSalary"
              @execute="putSalary"
            />
          </template>

          <template #editAsJson>
            <JsonEditor
              v-model:target-salary="targetSalary"
              @execute="putSalary"
            />
          </template>

          <template #uploadAndOcr>
            <PayslipUploader @upload="executeOcr" />
          </template>
        </Tabs>
      </Panel>
    </div>

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
  </div>
</template>

<script setup lang="ts">
import axios from "axios";

import { FileApi, SalaryApi } from "~/api/client";
import type { Overview, PayslipData, Structure } from "~/api/client";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Tabs from "~/components/common/Tabs.vue";
import MonthPicker from "~/components/common/MonthPicker.vue";
import Dialog from "~/components/common/Dialog.vue";
import FormEditor from "~/components/salary/edit/FormEditor.vue";
import JsonEditor from "~/components/salary/edit/JsonEditor.vue";
import PayslipUploader from "~/components/salary/edit/PayslipUploader.vue";
import {
  useInfoDialog,
  useConfirmDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import { getCurrentMonthFirstDateString } from "~/utils/date";
import { withErrorHandling } from "~/utils/api-call";
import { generateRandomString } from "~/utils/rand";

const fileApi = new FileApi();
const salaryApi = new SalaryApi();
const commonStore = useCommonStore();
const salaryStore = useSalaryStore();
const { showInfoDialog, infoDialogMessage, openInfoDialog, onInfoOk } =
  useInfoDialog();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();

onMounted(async () => {
  await fetchSalary();
});

const fetchSalary = async () => {
  const result = await withErrorHandling(
    async () => salaryStore.fetchSalary(targetDate.value),
    commonStore
  );
  if (result) {
    commonStore.setHasUnsavedChange(false);
  }
};

const targetDate = ref<string>(getCurrentMonthFirstDateString());
const tempDate = ref<string>(targetDate.value);
const onChangeDate = async (value: string | undefined) => {
  if (!value) {
    return;
  }
  tempDate.value = value;
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      "You Have Unsaved Change. Continue?"
    );
    if (confirmed) {
      targetDate.value = tempDate.value;
      fetchSalary();
    } else {
      tempDate.value = targetDate.value;
    }
  } else {
    targetDate.value = tempDate.value;
    await fetchSalary();
  }
};
const target = computed(() => salaryStore.salaries?.[0] ?? undefined);
const targetSalary = ref<{
  id: string;
  overview: Overview;
  structure: Structure;
  payslipData: PayslipData[];
}>({
  id: generateRandomString(),
  overview: {
    grossIncome: 0,
    netIncome: 0,
    operatingTime: 0,
    overtime: 0,
    bonus: 0,
    bonusTakeHome: 0,
    ...target.value?.overview,
  },
  structure: {
    basicSalary: 0,
    overtimePay: 0,
    housingAllowance: 0,
    positionAllowance: 0,
    other: 0,
    ...target.value?.structure,
  },
  payslipData: structuredClone(toRaw(target.value?.payslipData ?? [])),
});
watch(
  target,
  () => {
    targetSalary.value = {
      id: generateRandomString(),
      overview: {
        grossIncome: 0,
        netIncome: 0,
        operatingTime: 0,
        overtime: 0,
        bonus: 0,
        bonusTakeHome: 0,
        ...target.value?.overview,
      },
      structure: {
        basicSalary: 0,
        overtimePay: 0,
        housingAllowance: 0,
        positionAllowance: 0,
        other: 0,
        ...target.value?.structure,
      },
      payslipData: structuredClone(toRaw(target.value?.payslipData ?? [])),
    };
  },
  { immediate: true }
);

const putSalary = async () => {
  const result = await withErrorHandling(async () => {
    await salaryStore.putSalary({
      ...target.value,
      targetDate: targetDate.value,
      overview: { ...targetSalary.value.overview },
      structure: { ...targetSalary.value.structure },
      payslipData: structuredClone(
        toRaw(targetSalary.value?.payslipData ?? []).map((e) =>
          toRaw({ ...e, data: e.data.map((e2) => toRaw(e2)) })
        )
      ),
    });
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    await openInfoDialog("Process Completed Successfully");
  }
};

const executeOcr = async (file: File) => {
  const result = await withErrorHandling(async () => {
    const res = await fileApi.getUploadUrl();
    const { fileId, uploadUrl } = res.data;
    if (!uploadUrl) {
      throw new TypeError(`uploadUrl is invalid: ${uploadUrl}`);
    }
    await axios.put(uploadUrl, file, {
      headers: {
        "Content-Type": "application/pdf",
      },
    });
    await salaryApi.getSalaryOcr(targetDate.value, fileId);
  }, commonStore);
  if (result) {
    await openInfoDialog("Process Completed Successfully");
    await fetchSalary();
  }
};
</script>
