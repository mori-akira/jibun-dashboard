<template>
  <div>
    <Breadcrumb
      :items="[
        {
          text: 'Salary',
          iconName: 'tabler:report-money',
          link: `/share/${token}/salary`,
        },
        { text: 'Payslip', iconName: 'tabler:align-box-left-top' },
      ]"
    />

    <div
      v-if="status === 'forbidden'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:lock" class="text-5xl" />
      <p>This share link does not include salary data.</p>
    </div>

    <div
      v-else-if="status === 'gone'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:clock-off" class="text-5xl" />
      <p>This share link has expired.</p>
    </div>

    <template v-else>
      <div class="flex justify-between">
        <Panel wrapper-class="w-full">
          <MonthPickerFromTo
            label="Scope"
            :date-from="dateFrom"
            :date-to="dateTo"
            label-class="w-20 font-cursive"
            pickers-wrapper-class="min-w-96 w-1/2"
            @change:from="onChangeDateFrom"
            @change:to="onChangeDateTo"
          />
        </Panel>
      </div>

      <template
        v-for="year in getFinancialYears(
          payslipSalaries,
          financialYearStartMonth,
        ).toReversed()"
        :key="`fy-${year}`"
      >
        <div class="flex justify-between">
          <Panel wrapper-class="w-full overflow-x-auto">
            <h3>
              <span class="font-cursive font-bold ml-2">{{ `FY${year}` }}</span>
            </h3>
            <template
              v-for="(chunk, i) in chunkArray(
                filterSalaryByFinancialYear(
                  payslipSalaries,
                  year,
                  financialYearStartMonth,
                ).toReversed(),
                3,
              )"
              :key="`chunk-${year}-${i}`"
            >
              <div class="h-128 flex justify-between items-center px-4 py-2">
                <template
                  v-for="(salary, j) in padArray(chunk, 3, undefined)"
                  :key="salary?.salaryId ?? `empty-${year}-${j}`"
                >
                  <Payslip
                    v-if="salary"
                    :salary="salary"
                    wrapper-class="w-full h-full"
                    title-class="font-cursive font-bold"
                    headline-class="font-cursive"
                    label-class="font-cursive"
                  />
                  <div v-if="!salary" class="min-w-82 w-full h-full m-4"></div>
                </template>
              </div>
            </template>
          </Panel>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { AxiosError } from "axios";
import type { Ref } from "vue";
import type { Setting } from "~/generated/api/client";
import { useApiClient } from "~/composables/common/useApiClient";
import { useSalaryStore } from "~/stores/salary";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import MonthPickerFromTo from "~/components/common/MonthPickerFromTo.vue";
import Payslip from "~/components/salary/Payslip.vue";
import { getFinancialYears, filterSalaryByFinancialYear } from "~/utils/salary";
import { chunkArray, padArray } from "~/utils/array";

definePageMeta({ layout: "share" });

const route = useRoute();
const token = route.params.token as string;
const { getShareApi } = useApiClient();
const salaryStore = useSalaryStore();

const status = ref<"ok" | "forbidden" | "gone">("ok");
const shareSetting = ref<Setting | null>(null);
const shareStatus = inject<Ref<"ok" | "gone">>("shareStatus")!;
const forbiddenTypes = inject<Ref<string[]>>("forbiddenTypes")!;

onMounted(async () => {
  try {
    if (!salaryStore.salaries) {
      const salaryRes = await getShareApi().getShareSalaries(token);
      salaryStore.salaries = salaryRes.data;
    }
    const settingRes = await getShareApi().getShareSetting(token);
    shareSetting.value = settingRes.data;
  } catch (err) {
    if (err instanceof AxiosError) {
      if (err.response?.status === 403) {
        status.value = "forbidden";
        forbiddenTypes.value = [...forbiddenTypes.value, "salary"];
      } else if (err.response?.status === 410) status.value = "gone";
    }
  }
  if (status.value === "gone") shareStatus.value = "gone";
});

const financialYearStartMonth = computed(
  () => shareSetting.value?.salary.financialYearStartMonth ?? 1,
);

const dateFrom = ref<string | undefined>("");
const dateTo = ref<string | undefined>("");
const payslipSalaries = computed(() =>
  (salaryStore.salaries ?? []).filter((s) => {
    if (dateFrom.value && s.targetDate < dateFrom.value) return false;
    if (dateTo.value && s.targetDate > dateTo.value) return false;
    return true;
  }),
);
const onChangeDateFrom = (value: string | undefined) => {
  dateFrom.value = value;
};
const onChangeDateTo = (value: string | undefined) => {
  dateTo.value = value;
};
</script>
