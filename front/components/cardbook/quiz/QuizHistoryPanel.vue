<template>
  <Panel :wrapper-class="panelWrapperClass">
    <h3>
      <Icon name="tabler:list" class="adjust-icon-4" />
      <span class="font-cursive font-bold ml-2">History</span>
    </h3>
    <DataTable
      :rows="rows"
      :column-defs="filteredColumnDefs"
      :is-loading="isLoading"
      :init-sort-state="initSortState"
      :page-size="paginationPageSize"
      :wrapper-class="tableWrapperClass"
      header-class="font-cursive h-8 bg-gray-800 text-white"
      row-clickable
      @click:row="onClickRow"
    />
  </Panel>

  <!-- Detail Modal -->
  <ModalWindow
    :show-modal="selectedRow !== null"
    :modal-box-class="modalBoxClass"
    @close="selectedRow = null"
  >
    <template v-if="selectedRow">
      <div class="flex justify-end">
        <IconButton
          type="cancel"
          icon-class="w-6 h-6"
          @click:button="selectedRow = null"
        />
      </div>
      <h3 class="font-cursive font-bold mb-4">Quiz History Detail</h3>

      <!-- Summary -->
      <InformationForm
        :item="selectedRow"
        :item-defs="summaryItemDefs"
        :wrapper-class="summaryWrapperClass"
        :label-class="summaryLabelClass"
        item-class="bg-gray-200 flex-1"
      />

      <!-- Answer list -->
      <h4 class="font-cursive font-bold mt-6 mb-2 ml-1">Answers</h4>
      <div class="flex flex-col gap-1">
        <div
          v-for="answer in selectedAnswers"
          :key="answer.cardId"
          class="inline-flex items-center gap-2 px-2 py-1 rounded self-start"
          :class="answer.result === 'CORRECT' ? 'bg-green-50' : 'bg-red-50'"
        >
          <Icon
            :name="
              answer.result === 'CORRECT'
                ? 'tabler:circle-check'
                : 'tabler:circle-x'
            "
            :class="
              answer.result === 'CORRECT' ? 'text-green-600' : 'text-red-500'
            "
            class="shrink-0 text-lg"
          />
          <span class="text-sm">{{ answer.cardFront }}</span>
        </div>
      </div>
    </template>
  </ModalWindow>
</template>

<script setup lang="ts">
import type { CardbookQuizHistory } from "~/generated/api/client/api";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import IconButton from "~/components/common/IconButton.vue";
import { useCardbookStore } from "~/stores/cardbook";
import { formatToJST } from "~/utils/date";
import { getCardbookDirectionLabel } from "~/utils/cardbookQuiz";

const props = defineProps<{
  isLoading: boolean;
  panelWrapperClass?: string;
  tableWrapperClass?: string;
  visibleFields?: string[];
  modalBoxClass?: string;
  summaryWrapperClass?: string;
  summaryLabelClass?: string;
}>();

const cardbookStore = useCardbookStore();

type HistoryRow = CardbookQuizHistory & {
  answeredAtFormatted: string;
  directionLabel: string;
} & Record<string, unknown>;

const allColumnDefs: ColumnDef<HistoryRow>[] = [
  {
    field: "answeredAtFormatted",
    header: "Answered At",
    sortable: true,
    headerClass: "min-w-48 w-48 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "questionCount",
    header: "Questions",
    sortable: true,
    headerClass: "min-w-24 w-24 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "directionLabel",
    header: "Direction",
    sortable: true,
    headerClass: "min-w-36 w-36 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "correctCount",
    header: "Correct",
    sortable: true,
    headerClass: "min-w-24 w-24 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "incorrectCount",
    header: "Incorrect",
    sortable: true,
    headerClass: "min-w-24 w-24 text-center",
    bodyClass: "h-12 text-center",
  },
];

const filteredColumnDefs = computed(() =>
  props.visibleFields
    ? allColumnDefs.filter((c) =>
        props.visibleFields!.includes(c.field as string),
      )
    : allColumnDefs,
);

const initSortState: SortDef<HistoryRow> = {
  column: "answeredAtFormatted",
  direction: "desc",
};

const paginationPageSize = computed(() =>
  rows.value.length > 100 ? [100, 300, 500] : undefined,
);

const cardFrontMap = computed(() => {
  const map = new Map<string, string>();
  for (const c of cardbookStore.cardbookCards ?? []) {
    map.set(c.cardId ?? "", c.front);
  }
  return map;
});

const rows = computed<HistoryRow[]>(() =>
  (cardbookStore.quizHistories ?? []).map((h, i) => ({
    ...h,
    index: i + 1,
    answeredAtFormatted: formatToJST(h.answeredAt) ?? "",
    directionLabel: getCardbookDirectionLabel(
      h.direction as "FRONT_TO_BACK" | "BACK_TO_FRONT",
    ),
  })),
);

const selectedRow = ref<HistoryRow | null>(null);

const summaryItemDefs: ItemDef[] = [
  { field: "answeredAtFormatted", label: "Answered At", skipIfNull: true },
  { field: "questionCount", label: "Questions", skipIfNull: true },
  { field: "directionLabel", label: "Direction", skipIfNull: true },
  { field: "correctCount", label: "Correct", skipIfNull: true },
  { field: "incorrectCount", label: "Incorrect", skipIfNull: true },
];

const selectedAnswers = computed(() => {
  if (!selectedRow.value) return [];
  return (selectedRow.value.answers ?? []).map((a) => ({
    cardId: a.cardId,
    cardFront: cardFrontMap.value.get(a.cardId) ?? a.cardId,
    result: a.result,
  }));
});

function onClickRow(row: HistoryRow) {
  selectedRow.value = row;
}
</script>
