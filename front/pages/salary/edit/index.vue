<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Salary', iconName: 'tabler:report-money', link: '/salary' },
        { text: 'Edit', iconName: 'tabler:database-edit' },
      ]"
    />

    <div class="flex justify-center items-center">
      <Panel panel-class="w-2/3">
        <div class="flex justify-between">
          <MonthPicker
            label="Target"
            month-picker
            :date="tempDate"
            label-class="w-20 font-cursive"
            pickers-wrapper-class="min-w-40 w-1/5"
            @change="onChangeDate"
          />
          <Button
            type="delete"
            size="small"
            wrapper-class="mx-8 min-w-24"
            :disabled="target === undefined"
            @click="onDeleteSalary"
          >
            <Icon name="tabler:trash" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Delete</span>
          </Button>
        </div>
      </Panel>
    </div>

    <div class="flex justify-center">
      <div class="relative w-2/3">
        <Panel panel-class="w-full !m-0">
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
                @execute="onPutSalary"
              />
            </template>

            <template #editAsJson>
              <JsonEditor
                v-model:target-salary="targetSalary"
                @execute="onPutSalary"
              />
            </template>

            <template #uploadAndOcr>
              <MessageBox
                v-show="lastOcrTaskResult?.status === 'COMPLETED'"
                type="confirm"
                wrapper-class="mt-4 p-2"
              >
                <Icon
                  name="tabler:circle-check-filled"
                  class="text-2xl text-green-800"
                />
                <span class="pl-2 font-bold font-cursive text-green-800"
                  >Task Completed Successfully</span
                >
              </MessageBox>
              <MessageBox
                v-show="lastOcrTaskResult?.status === 'FAILED'"
                type="error"
                wrapper-class="mt-4 p-2"
              >
                <Icon
                  name="tabler:exclamation-circle-filled"
                  class="text-2xl text-red-800"
                />
                <span class="pl-2 font-bold font-cursive text-red-800"
                  >Task Failed</span
                >
              </MessageBox>
              <PayslipUploader @upload="onExecuteOcr" />
            </template>
          </Tabs>
        </Panel>
        <LoadingOverlay
          :is-loading="isRunningSalaryOcrTask"
          :fullscreen="false"
          message="Loading Payslip..."
          wrapper-class="rounded-lg !bg-black/50"
        />
      </div>
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
import { useI18n } from "vue-i18n";

import { Configuration } from "~/generated/api/client/configuration";
import { FileApi, SalaryApi } from "~/generated/api/client";
import type { Overview, PayslipData, Structure } from "~/generated/api/client";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import Tabs from "~/components/common/Tabs.vue";
import MonthPicker from "~/components/common/MonthPicker.vue";
import Dialog from "~/components/common/Dialog.vue";
import MessageBox from "~/components/common/MessageBox.vue";
import LoadingOverlay from "~/components/common/LoadingOverlay.vue";
import FormEditor from "~/components/salary/edit/FormEditor.vue";
import JsonEditor from "~/components/salary/edit/JsonEditor.vue";
import PayslipUploader from "~/components/salary/edit/PayslipUploader.vue";
import {
  useInfoDialog,
  useConfirmDialog,
} from "~/composables/common/useDialog";
import { useAuth } from "~/composables/common/useAuth";
import { usePolling } from "~/composables/common/usePolling";
import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import { getCurrentMonthFirstDateString } from "~/utils/date";
import { withErrorHandling } from "~/utils/api-call";
import { generateRandomString } from "~/utils/rand";

const { t } = useI18n();
const { getAccessToken } = useAuth();
const configuration = new Configuration({
  baseOptions: {
    headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
  },
});
const fileApi = new FileApi(configuration);
const salaryApi = new SalaryApi(configuration);
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
const {
  isActive: isOcrPollingActive,
  lastResult: lastOcrRunning,
  start: startOcrPolling,
} = usePolling<boolean>({
  intervalMs: 3000,
  timeoutMs: 3 * 60 * 1000,
  immediate: true,
  async onPoll() {
    try {
      const running = salaryStore.isRunningSalaryOcrTask(targetDate.value);
      return running;
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
      throw err;
    }
  },
  shouldStop: ({ result }) => !result,
  onTimeout: () => {
    commonStore.addErrorMessage(t("message.error.timeout"));
  },
});

onMounted(async () => {
  await fetchSalary();
});

const fetchSalary = async () => {
  const result = await withErrorHandling(
    async () => await salaryStore.fetchSalary(targetDate.value),
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
      t("message.confirm.checkUnsavedChanges")
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

const isRunningSalaryOcrTask = computed(
  () => isOcrPollingActive.value && lastOcrRunning.value !== false
);
watch(targetDate, async () => await startOcrPolling(), { immediate: true });
watch(
  () => lastOcrRunning.value,
  async (running, prev) => {
    if (prev === true && running === false) {
      await fetchSalary();
    }
  }
);
const lastOcrTaskResult = computed(() => {
  const tasks = salaryStore.salaryOcrTasks ?? [];
  if (tasks.length === 0) return undefined;
  return tasks.reduce((latest, t) => {
    const lt = new Date(latest.updatedAt).getTime();
    const tt = new Date(t.updatedAt).getTime();
    if (tt > lt) return t;
    return latest;
  });
});

const onPutSalary = async () => {
  const result = await withErrorHandling(async () => {
    await salaryStore.putSalary(target.value?.salaryId, {
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
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchSalary();
  }
};

const onExecuteOcr = async (file: File) => {
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
    await salaryApi.postSalaryOcrTasksStart({
      targetDate: targetDate.value,
      fileId,
    });
  }, commonStore);
  if (result) {
    await openInfoDialog(t("message.info.acceptSuccessfully"));
    await startOcrPolling();
  }
};

const onDeleteSalary = async () => {
  const confirmed = await openConfirmDialog(t("message.confirm.deleteSalary"));
  if (!confirmed) {
    return;
  }
  if (!target.value || !target.value?.salaryId) {
    return;
  }
  const result = await withErrorHandling(async () => {
    await salaryStore.deleteSalary(target.value?.salaryId ?? "");
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchSalary();
  }
};
</script>
