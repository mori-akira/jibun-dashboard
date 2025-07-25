<template>
  <div>
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
              :key="`${targetSalary.id}-overview.${fieldDef.key}`"
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
              :key="`${targetSalary.id}-structure.${fieldDef.key}`"
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
                :key="`${targetSalary.id}-payslip.${payslip.key}`"
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
                  () => onDeletePayslipData(payslipCategory.key, payslip.key)
                "
              />
            </div>
          </template>
          <div class="ml-4 mt-2 w-full flex justify-center">
            <div class="w-40">
              <Button
                type="add"
                size="small"
                @click="onAddNewPayslipKey(payslipCategory.key)"
              >
                <Icon name="tabler:plus" class="text-base translate-y-0.5" />
                <span class="ml-2">Add Payslip</span>
              </Button>
            </div>
            <div class="w-76" />
          </div>
          <div class="w-full flex justify-center mt-8 pb-2">
            <Button
              type="delete"
              size="small"
              @click="onDeleteCategory(payslipCategory.key)"
            >
              <Icon name="tabler:trash" class="text-base translate-y-0.5" />
              <span class="ml-2">Delete Category</span>
            </Button>
          </div>
        </Accordion>
      </template>
      <div class="flex justify-start ml-4">
        <Button type="add" size="small" @click="onAddCategory">
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="ml-2">Add Category</span>
        </Button>
      </div>

      <div class="w-full flex justify-center mt-8">
        <Button
          :disabled="!meta?.valid"
          type="action"
          wrapper-class="flex justify-center"
          @click="onExecute"
        >
          <Icon name="tabler:database-share" class="adjust-icon-4" />
          <span class="ml-2">Execute</span>
        </Button>
      </div>
    </Form>

    <Dialog
      :show-dialog="showInputDialog"
      type="input"
      :message="inputDialogMessage"
      button-type="okCancel"
      input-class="text-center"
      @click:ok="onInputOk"
      @click:cancel="onInputCancel"
      @close="onInputCancel"
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
import { Form, Field } from "vee-validate";

import type { Overview, PayslipData, Structure } from "~/api/client";
import { schemas } from "~/api/client/schemas";
import Accordion from "~/components/common/Accordion.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import Dialog from "~/components/common/Dialog.vue";
import {
  useConfirmDialog,
  useInputDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

const props = defineProps<{
  targetSalary: {
    id: string;
    overview: Overview;
    structure: Structure;
    payslipData: PayslipData[];
  };
}>();
const emit = defineEmits<{
  (
    e: "update:targetSalary",
    value: {
      id: string;
      overview: Overview;
      structure: Structure;
      payslipData: PayslipData[];
    }
  ): void;
  (e: "execute"): void;
}>();

const commonStore = useCommonStore();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();
const {
  showInputDialog,
  inputDialogMessage,
  openInputDialog,
  onInputOk,
  onInputCancel,
} = useInputDialog();
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
  const targetElement = e.target as HTMLInputElement;
  const parsed = Number(targetElement.value);
  const newSalary = {
    ...props.targetSalary,
    overview: {
      ...props.targetSalary.overview,
      [key]: Number.isFinite(parsed) ? parsed : 0,
    },
  };
  emit("update:targetSalary", newSalary);
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
  const targetElement = e.target as HTMLInputElement;
  const parsed = Number(targetElement.value);
  const newSalary = {
    ...props.targetSalary,
    structure: {
      ...props.targetSalary.structure,
      [key]: Number.isFinite(parsed) ? parsed : 0,
    },
  };
  emit("update:targetSalary", newSalary);
  commonStore.setHasUnsavedChange(true);
};

const onAddCategory = async () => {
  const newCategory = await openInputDialog("Input New Category Name");
  if (newCategory) {
    const newSalary = {
      ...props.targetSalary,
      payslipData: [
        ...props.targetSalary.payslipData,
        { key: newCategory, data: [] },
      ],
    };
    emit("update:targetSalary", newSalary);
    commonStore.setHasUnsavedChange(true);
  }
};
const onDeleteCategory = async (category: string) => {
  const confirmed = await openConfirmDialog(
    "Confirm Deletion Of The Category?"
  );
  if (confirmed) {
    const newSalary = {
      ...props.targetSalary,
      payslipData: props.targetSalary.payslipData.filter(
        (e) => e.key !== category
      ),
    };
    emit("update:targetSalary", newSalary);
    commonStore.setHasUnsavedChange(true);
  }
};

const onAddNewPayslipKey = async (category: string) => {
  const newPayslipKey = await openInputDialog("Input New Payslip");
  if (newPayslipKey) {
    const newPayslipData = props.targetSalary.payslipData.map((e) =>
      e.key === category
        ? {
            ...e,
            data: [...e.data, { key: newPayslipKey, data: 0 }],
          }
        : e
    );
    emit("update:targetSalary", {
      ...props.targetSalary,
      payslipData: newPayslipData,
    });
    commonStore.setHasUnsavedChange(true);
  }
};
const onChangePayslipData = (e: Event, category: string, key: string) => {
  const input = e.target as HTMLInputElement;
  const val = Number(input.value);
  const newPayslipData = props.targetSalary.payslipData.map((c) =>
    c.key === category
      ? {
          ...c,
          data: c.data.map((p) =>
            p.key === key ? { ...p, data: Number.isFinite(val) ? val : 0 } : p
          ),
        }
      : c
  );
  emit("update:targetSalary", {
    ...props.targetSalary,
    payslipData: newPayslipData,
  });
  commonStore.setHasUnsavedChange(true);
};
const onDeletePayslipData = (category: string, key: string) => {
  const newPayslipData = props.targetSalary.payslipData.map((c) =>
    c.key === category
      ? {
          ...c,
          data: c.data.filter((p) => p.key !== key),
        }
      : c
  );
  emit("update:targetSalary", {
    ...props.targetSalary,
    payslipData: newPayslipData,
  });
  commonStore.setHasUnsavedChange(true);
};

const onExecute = () => {
  emit("execute");
};
</script>
