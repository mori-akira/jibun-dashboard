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
            <Form v-slot="{ meta }">
              <Accordion
                title="Overview"
                title-class="font-cursive"
                wrapper-class="m-4"
              >
                <template
                  v-for="fieldDef in overviewFields"
                  :key="`${targetSalary.id}-overview.${fieldDef.key}`"
                >
                  <div class="w-full flex justify-center">
                    <Field
                      v-slot="{ field, errorMessage, validate }"
                      :name="`overview.${fieldDef.key}`"
                      :rules="validationRules.overview[fieldDef.key]"
                      :value="targetSalary.overview[fieldDef.key]?.toString()"
                    >
                      <TextBox
                        :label="fieldDef.label"
                        v-bind="field"
                        :error-message="errorMessage"
                        type="number"
                        wrapper-class="mx-4 my-2"
                        label-class="w-40 ml-4 font-cursive"
                        input-wrapper-class="w-60"
                        input-class="text-center"
                        @blur:event="
                          async ($event) => {
                            field.onBlur($event);
                            const result = await validate();
                            if (result.valid) {
                              onChangeOverview($event, fieldDef.key);
                            }
                          }
                        "
                      />
                    </Field>
                  </div>
                </template>
              </Accordion>
              <Accordion
                title="Structure"
                title-class="font-cursive"
                wrapper-class="m-4"
              >
                <template
                  v-for="fieldDef in structureFields"
                  :key="`${targetSalary.id}-structure.${fieldDef.key}`"
                >
                  <div class="w-full flex justify-center">
                    <Field
                      v-slot="{ field, errorMessage, validate }"
                      :name="`structure.${fieldDef.key}`"
                      :rules="validationRules.structure[fieldDef.key]"
                      :value="targetSalary.structure[fieldDef.key]?.toString()"
                    >
                      <TextBox
                        :label="fieldDef.label"
                        v-bind="field"
                        :error-message="errorMessage"
                        type="number"
                        wrapper-class="mx-4 my-2"
                        label-class="w-40 ml-4 font-cursive"
                        input-wrapper-class="w-60"
                        input-class="text-center"
                        @blur:event="
                          async ($event) => {
                            field.onBlur($event);
                            const result = await validate();
                            if (result.valid) {
                              onChangeStructure($event, fieldDef.key);
                            }
                          }
                        "
                      />
                    </Field>
                  </div>
                </template>
              </Accordion>
              <template
                v-for="payslipCategory in targetSalary.payslipData"
                :key="`category-${payslipCategory.key}`"
              >
                <Accordion
                  :title="payslipCategory.key"
                  title-class="font-cursive"
                  wrapper-class="m-4"
                >
                  <template
                    v-for="payslip in payslipCategory.data"
                    :key="`${targetSalary.id}-payslip.${payslip.key}`"
                  >
                    <div class="w-full flex justify-center">
                      <Field
                        v-slot="{ field, errorMessage, validate }"
                        :name="`payslip.${payslip.key}`"
                        :rules="validationRules.payslipData"
                        :value="payslip.data.toString()"
                      >
                        <TextBox
                          :label="payslip.key"
                          v-bind="field"
                          :error-message="errorMessage"
                          type="number"
                          wrapper-class="mx-4 my-2"
                          label-class="w-40 ml-4 font-cursive"
                          input-wrapper-class="w-60"
                          input-class="text-center"
                          @blur:event="
                            async ($event) => {
                              field.onBlur($event);
                              const result = await validate();
                              if (result.valid) {
                                onChangePayslipData(
                                  $event,
                                  payslipCategory.key,
                                  payslip.key
                                );
                              }
                            }
                          "
                        />
                      </Field>
                      <IconButton
                        type="cancel"
                        wrapper-class="w-10 ml-2 flex items-center"
                        icon-class="justify-icon-2 text-color-gray-700"
                        @click="
                          () =>
                            onDeletePayslipData(
                              payslipCategory.key,
                              payslip.key
                            )
                        "
                      />
                    </div>
                  </template>
                  <div class="mx-4 my-2 w-full flex justify-center">
                    <div class="w-40 flex justify-start">
                      <TextBox
                        :value="newPayslipKeys[payslipCategory.key]"
                        input-wrapper-class="w-40"
                        @blur:value="
                          ($value) =>
                            onChangeNewPayslipKey($value, payslipCategory.key)
                        "
                      />
                      <IconButton
                        type="plus"
                        wrapper-class="ml-2 flex items-center"
                        icon-class="justify-icon-2 text-color-gray-700"
                        @click="() => onAddNewPayslipKey(payslipCategory.key)"
                      />
                    </div>
                    <div class="w-84" />
                  </div>
                  <div class="w-full flex justify-center mt-8 pb-2">
                    <Button
                      type="delete"
                      size="small"
                      @click="onDeleteCategory(payslipCategory.key)"
                    >
                      <Icon
                        name="tabler:trash"
                        class="text-base translate-y-0.5"
                      />
                      <span class="ml-2">Delete Category</span>
                    </Button>
                  </div>
                </Accordion>
              </template>
              <div class="flex justify-start ml-4">
                <TextBox
                  :value="newCategory"
                  input-wrapper-class="w-40"
                  @blur:value="onChangeNewCategory"
                />
                <IconButton
                  type="plus"
                  wrapper-class="ml-2 flex items-center"
                  icon-class="justify-icon-2 text-color-gray-700"
                  @click="onAddCategory"
                />
              </div>

              <div class="w-full flex justify-center mt-8">
                <Button
                  :disabled="!meta?.valid"
                  type="action"
                  wrapper-class="flex justify-center"
                  @click="putSalary"
                >
                  <Icon name="tabler:database-share" class="adjust-icon-4" />
                  <span class="ml-2">Execute</span>
                </Button>
              </div>
            </Form>
          </template>

          <template #editAsJson>
            <p>Edit As JSON</p>
          </template>

          <template #uploadAndOcr>
            <p>Upload And OCR</p>
          </template>
        </Tabs>
      </Panel>
    </div>

    <Dialog
      :show-dialog="showCompleteDialog"
      type="info"
      message="Process Completed Successfully"
      button-type="ok"
      @click:ok="() => (showCompleteDialog = false)"
      @close="() => (showCompleteDialog = false)"
    />

    <Dialog
      :show-dialog="showConfirmDialog"
      type="confirm"
      :message="dialogMessage"
      button-type="yesNo"
      @click:yes="onYes"
      @click:no="onNo"
      @close="onNo"
    />
  </div>
</template>

<script setup lang="ts">
import { Form, Field } from "vee-validate";

import type { Overview, PayslipData, Structure } from "~/api/client";
import { schemas } from "~/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Tabs from "~/components/common/Tabs.vue";
import Accordion from "~/components/common/Accordion.vue";
import MonthPicker from "~/components/common/MonthPicker.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useConfirmDialog } from "~/composables/useConfirmDialog";
import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import { generateRandomString } from "~/utils/rand";
import { getCurrentMonthFirstDateString } from "~/utils/date";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

const commonStore = useCommonStore();
commonStore.setHasUnsavedChange(false);
const salaryStore = useSalaryStore();
const { showConfirmDialog, dialogMessage, openConfirmDialog, onYes, onNo } =
  useConfirmDialog();
const validationRules = {
  overview: {
    grossIncome: zodToVeeRules(schemas.Salary.shape.overview.shape.grossIncome),
    netIncome: zodToVeeRules(schemas.Salary.shape.overview.shape.grossIncome),
    operatingTime: zodToVeeRules(
      schemas.Salary.shape.overview.shape.grossIncome
    ),
    overtime: zodToVeeRules(schemas.Salary.shape.overview.shape.grossIncome),
    bonus: zodToVeeRules(schemas.Salary.shape.overview.shape.grossIncome),
    bonusTakeHome: zodToVeeRules(
      schemas.Salary.shape.overview.shape.grossIncome
    ),
  },
  structure: {
    basicSalary: zodToVeeRules(
      schemas.Salary.shape.structure.shape.basicSalary
    ),
    overtimePay: zodToVeeRules(
      schemas.Salary.shape.structure.shape.overtimePay
    ),
    housingAllowance: zodToVeeRules(
      schemas.Salary.shape.structure.shape.housingAllowance
    ),
    positionAllowance: zodToVeeRules(
      schemas.Salary.shape.structure.shape.positionAllowance
    ),
    other: zodToVeeRules(schemas.Salary.shape.structure.shape.other),
  },
  payslipData: zodToVeeRules(
    schemas.Salary.shape.payslipData._def.type.shape.data._def.type.shape.data
  ),
};

onMounted(async () => {
  await fetchSalary();
});

const fetchSalary = async () => {
  const id = commonStore.addLoadingQueue();
  await salaryStore.fetchSalary(targetDate.value);
  commonStore.deleteLoadingQueue(id);
  commonStore.setHasUnsavedChange(false);
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

const overviewFields: {
  key: keyof Overview;
  label: string;
}[] = [
  { key: "grossIncome", label: "Gross Income" },
  { key: "netIncome", label: "Net Income" },
  { key: "operatingTime", label: "Operating Time" },
  { key: "overtime", label: "Overtime" },
  { key: "bonus", label: "Bonus" },
  { key: "bonusTakeHome", label: "Bonus Take Home" },
];
const onChangeOverview = (e: Event, key: keyof Overview) => {
  const target = e.target as HTMLInputElement;
  const parsed = Number(target.value);
  targetSalary.value.overview[key] = Number.isFinite(parsed) ? parsed : 0;
  commonStore.setHasUnsavedChange(true);
};
const structureFields: {
  key: keyof Structure;
  label: string;
}[] = [
  { key: "basicSalary", label: "Basic Salary" },
  { key: "overtimePay", label: "Overtime Pay" },
  { key: "housingAllowance", label: "Housing Allowance" },
  { key: "positionAllowance", label: "Positional Allowance" },
  { key: "other", label: "Other" },
];
const onChangeStructure = (e: Event, key: keyof Structure) => {
  const target = e.target as HTMLInputElement;
  const parsed = Number(target.value);
  targetSalary.value.structure[key] = Number.isFinite(parsed) ? parsed : 0;
  commonStore.setHasUnsavedChange(true);
};

const newCategory = ref<string>("");
const onChangeNewCategory = (val: string) => {
  newCategory.value = val;
};
const onAddCategory = () => {
  if (newCategory.value) {
    targetSalary.value.payslipData.push({
      key: newCategory.value,
      data: [],
    });
    newCategory.value = "";
    commonStore.setHasUnsavedChange(true);
  }
};
const onDeleteCategory = async (category: string) => {
  const confirmed = await openConfirmDialog(
    "Confirm Deletion Of The Category?"
  );
  if (confirmed) {
    targetSalary.value.payslipData = targetSalary.value.payslipData.filter(
      (e) => e.key !== category
    );
    commonStore.setHasUnsavedChange(true);
  }
};

const newPayslipKeys = reactive<{ [x: string]: string }>({});
watch(
  targetSalary,
  () => {
    const newKeys: { [x: string]: string } = {};
    targetSalary.value.payslipData.forEach((e) => (newKeys[e.key] = ""));
    Object.assign(newPayslipKeys, newKeys);
  },
  { immediate: true }
);
const onChangeNewPayslipKey = (newVal: string, category: string) => {
  if (category in newPayslipKeys) {
    newPayslipKeys[category] = newVal;
  }
};
const onAddNewPayslipKey = (category: string) => {
  const payslipCategory = targetSalary.value.payslipData.filter(
    (e) => e.key === category
  )?.[0];
  if (payslipCategory && newPayslipKeys?.[category]) {
    payslipCategory.data.push({
      key: newPayslipKeys[category],
      data: 0,
    });
    newPayslipKeys[category] = "";
    commonStore.setHasUnsavedChange(true);
  }
};
const onChangePayslipData = (e: Event, category: string, key: string) => {
  const input = e.target as HTMLInputElement;
  const val = Number(input.value);
  const categoryObj = targetSalary.value.payslipData.find(
    (c) => c.key === category
  );
  const payslip = categoryObj?.data.find((p) => p.key === key);
  if (payslip) {
    payslip.data = Number.isFinite(val) ? val : 0;
    commonStore.setHasUnsavedChange(true);
  }
};
const onDeletePayslipData = (category: string, key: string) => {
  const payslipCategory = targetSalary.value.payslipData.find(
    (e) => e.key === category
  );
  if (payslipCategory) {
    payslipCategory.data = payslipCategory.data.filter((e) => e.key !== key);
    commonStore.setHasUnsavedChange(true);
  }
};

const putSalary = async () => {
  const id = commonStore.addLoadingQueue();
  try {
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
    showCompleteDialog.value = true;
    commonStore.setHasUnsavedChange(false);
  } catch (error) {
    console.error(error);
    commonStore.addErrorMessage(getErrorMessage(error));
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
};
const showCompleteDialog = ref<boolean>(false);
</script>
