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
        <DatePicker
          label="Target"
          month-picker
          :date="date"
          label-class="w-20 font-cursive"
          pickers-wrapper-class="min-w-36 w-1/5"
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
              <div class="flex justify-center">
                <Accordion
                  title="Overview"
                  title-class="font-cursive"
                  wrapper-class="w-full m-4"
                >
                  <template
                    v-for="fieldDef in overviewFields"
                    :key="`${targetSalary.id}-overview.${fieldDef.key}`"
                  >
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
                        wrapper-class="m-4 w-full justify-center"
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
                  </template>
                </Accordion>
              </div>
              <div class="w-full flex justify-center">
                <Button
                  :disabled="!meta?.valid"
                  type="action"
                  wrapper-class="flex justify-center"
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
import DatePicker from "~/components/common/DatePicker.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import { generateRandomString } from "~/utils/rand";
import { getCurrentMonthFirstDateString } from "~/utils/date";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

const commonStore = useCommonStore();
const salaryStore = useSalaryStore();
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
};

onMounted(async () => {
  await fetchSalary();
});

const fetchSalary = async () => {
  const id = generateRandomString();
  commonStore.addLoadingQueue(id);
  await salaryStore.fetchSalary(date.value);
  commonStore.deleteLoadingQueue(id);
};

const date = ref<string>(getCurrentMonthFirstDateString());
const onChangeDate = async (value: string | undefined) => {
  if (!value) {
    return;
  }
  date.value = value;
  await fetchSalary();
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
};
</script>
