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
            <Accordion
              title="Overview"
              title-class="font-cursive"
              wrapper-class="m-4"
            >
              <TextBox
                label="Gross Income"
                :value="targetSalary?.overview?.grossIncome?.toString() || ''"
                wrapper-class="m-4 w-full justify-start"
                label-class="w-56 ml-4 font-cursive"
                input-wrapper-class="w-48"
                input-class="px-4"
              />
            </Accordion>
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
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Tabs from "~/components/common/Tabs.vue";
import Accordion from "~/components/common/Accordion.vue";
import DatePicker from "~/components/common/DatePicker.vue";
import TextBox from "~/components/common/TextBox.vue";
import { useCommonStore } from "~/stores/common";
import { useSalaryStore } from "~/stores/salary";
import { getCurrentMonthFirstDateString } from "~/utils/date";
import { filterSalaryByFinancialYearMonth } from "~/utils/salary";

const commonStore = useCommonStore();
const salaryStore = useSalaryStore();

onMounted(async () => {
  const id = generateRandomString();
  commonStore.addLoadingQueue(id);
  await salaryStore.fetchSalary();
  commonStore.deleteLoadingQueue(id);
});

const date = ref<string>(getCurrentMonthFirstDateString());
const onChangeDate = async (value: string | undefined) => {
  if (!value) {
    return;
  }
  date.value = value;
};

const targetSalary = computed(() =>
  filterSalaryByFinancialYearMonth(salaryStore.salaries ?? [], date.value)
);
</script>
