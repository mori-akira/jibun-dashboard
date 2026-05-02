<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Vocabulary', iconName: 'tabler:book', link: '/vocabulary' },
        { text: 'Checks', iconName: 'tabler:shield-check' },
      ]"
    />

    <Panel>
      <DatePickerFromTo
        :date-from="checkedAtFrom"
        :date-to="checkedAtTo"
        label-class="w-24 font-cursive"
        pickers-wrapper-class="min-w-96 w-1/2"
        @change:from="onChangeCheckedAtFrom"
        @change:to="onChangeCheckedAtTo"
      />
    </Panel>

    <Panel wrapper-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:list" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Check Results</span>
      </h3>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        row-clickable
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
        @click:row="onClickRow"
      >
        <template #cell-severity="{ row }">
          <span
            :class="[
              'py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg',
              severityClass((row as CheckResultRow).severity),
            ]"
            >{{ (row as CheckResultRow).severity }}</span
          >
        </template>
        <template #cell-status="{ row }">
          <span
            :class="[
              'py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg',
              statusClass((row as CheckResultRow).status),
            ]"
            >{{ (row as CheckResultRow).status }}</span
          >
        </template>
      </DataTable>
    </Panel>

    <ModalWindow
      :show-modal="selectedResult !== null"
      modal-box-class="w-[60vw] max-w-3xl max-h-[80vh] overflow-y-auto"
      @close="selectedResult = null"
    >
      <template v-if="selectedResult">
        <div class="relative">
          <div class="flex justify-end">
            <IconButton
              type="cancel"
              icon-class="w-6 h-6"
              @click:button="selectedResult = null"
            />
          </div>

          <div class="flex flex-col gap-2 mx-4 mb-4">
            <div class="flex items-center justify-between">
              <span class="font-bold">{{ selectedResult.vocabularyName }}</span>
              <span class="text-sm text-gray-500 ml-auto"
                >Checked: {{ formatToJST(selectedResult.checkedAt) }}</span
              >
            </div>
            <div class="flex flex-wrap gap-2 items-center">
              <span
                :class="[
                  'py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg',
                  severityClass(selectedResult.severity),
                ]"
                >{{ selectedResult.severity }}</span
              >
              <span
                :class="[
                  'py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg',
                  statusClass(selectedResult.status),
                ]"
                >{{ selectedResult.status }}</span
              >
            </div>
          </div>

          <div class="mx-4 mt-8">
            <hr class="flex-1 border-t-2 border-gray-300" />
          </div>

          <div
            class="markdown-body bg-gray-50 rounded p-4 text-sm leading-relaxed"
            v-html="renderedReport"
          />

          <div
            v-if="selectedResult.status === 'UNCHECKED'"
            class="flex justify-end mt-4"
          >
            <Button type="action" size="small" @click:button="onMarkAsChecked">
              <Icon
                name="tabler:circle-check"
                class="text-base translate-y-0.5"
              />
              <span class="font-bold ml-2">Mark as Checked</span>
            </Button>
          </div>
        </div>
      </template>
    </ModalWindow>
  </div>
</template>

<script setup lang="ts">
import type { VocabularyCheckResult } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import DatePickerFromTo from "~/components/common/DatePickerFromTo.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { formatToJST } from "~/utils/date";
import { marked } from "marked";
import DOMPurify from "dompurify";

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

const checkedAtFrom = ref<string | undefined>(undefined);
const checkedAtTo = ref<string | undefined>(undefined);

const fetchCheckResults = async () => {
  await withLoading(async () => {
    try {
      await vocabularyStore.fetchCheckResults(
        checkedAtFrom.value,
        checkedAtTo.value,
      );
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
};

onMounted(fetchCheckResults);

const onChangeCheckedAtFrom = async (value: string | undefined) => {
  checkedAtFrom.value = value;
  await fetchCheckResults();
};

const onChangeCheckedAtTo = async (value: string | undefined) => {
  checkedAtTo.value = value;
  await fetchCheckResults();
};

type CheckResultRow = VocabularyCheckResult & { index: number } & Record<
    string,
    unknown
  >;

const columnDefs: ColumnDef<CheckResultRow>[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12",
  },
  {
    field: "vocabularyName",
    header: "Vocabulary",
    sortable: true,
    headerClass: "w-64",
    bodyClass: "h-12 truncate",
  },
  {
    field: "severity",
    header: "Severity",
    sortable: true,
    headerClass: "w-28",
    bodyClass: "h-12 text-center",
  },
  {
    field: "status",
    header: "Status",
    sortable: true,
    headerClass: "w-28",
    bodyClass: "h-12 text-center",
  },
  {
    field: "checkedAt",
    header: "Checked At",
    sortable: true,
    headerClass: "w-48",
    bodyClass: "h-12 text-sm text-center",
  },
];

const rows = computed<CheckResultRow[]>(() =>
  (vocabularyStore.checkResults ?? []).map((r, i) => ({
    ...r,
    index: i + 1,
    checkedAt: formatToJST(r.checkedAt),
  })),
);

const initSortState: SortDef<CheckResultRow> = {
  column: "checkedAt",
  direction: "desc",
};

const severityClass = (severity: string | undefined) => {
  if (severity === "HIGH") return "bg-red-500";
  if (severity === "MEDIUM") return "bg-yellow-500";
  return "bg-blue-400";
};

const statusClass = (status: string | undefined) =>
  status === "UNCHECKED" ? "bg-gray-400" : "bg-green-500";

const selectedResult = ref<VocabularyCheckResult | null>(null);

const renderedReport = computed(() =>
  selectedResult.value?.report
    ? DOMPurify.sanitize(marked(selectedResult.value.report) as string)
    : "",
);

const onClickRow = (row: CheckResultRow) => {
  const found = vocabularyStore.checkResults?.find(
    (r) => r.vocabularyCheckResultId === row.vocabularyCheckResultId,
  );
  selectedResult.value = found ?? null;
};

const onMarkAsChecked = async () => {
  if (!selectedResult.value?.vocabularyCheckResultId) return;
  const loadingId = commonStore.addLoadingQueue();
  try {
    await vocabularyStore.updateCheckResultStatus(
      selectedResult.value.vocabularyCheckResultId,
      { status: "CHECKED" },
    );
    await vocabularyStore.fetchCheckResults();
    selectedResult.value = null;
  } catch (err) {
    console.error(err);
    commonStore.addErrorMessage(getErrorMessage(err));
  } finally {
    commonStore.deleteLoadingQueue(loadingId);
  }
};
</script>
