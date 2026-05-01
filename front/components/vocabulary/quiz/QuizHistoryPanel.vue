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
      :wrapper-class="tableWrapperClass"
      header-class="font-cursive h-8 bg-gray-800 text-white"
    >
      <template #cell-tagNames="{ row }">
        <div class="flex flex-nowrap gap-1 overflow-hidden min-w-0">
          <span
            v-for="name in (row.tagNames as string[])"
            :key="name"
            class="shrink-0 py-[0.2rem] px-2 text-[0.8rem] text-white bg-[#888] rounded-lg"
            >{{ name }}</span
          >
          <span
            v-if="(row.tagNames as string[]).length === 0"
            class="text-gray-400 text-sm"
            >All</span
          >
        </div>
      </template>
    </DataTable>
  </Panel>
</template>

<script setup lang="ts">
import type { VocabularyQuizHistory } from "~/generated/api/client/api";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import { useVocabularyStore } from "~/stores/vocabulary";
import { formatToJST } from "~/utils/date";
import { getDirectionLabel } from "~/utils/vocabularyQuiz";

const props = defineProps<{
  isLoading: boolean;
  panelWrapperClass?: string;
  tableWrapperClass?: string;
  visibleFields?: string[];
}>();

const vocabularyStore = useVocabularyStore();

type HistoryRow = VocabularyQuizHistory & {
  answeredAtFormatted: string;
  tagNames: string[];
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
    field: "tagNames",
    header: "Tags",
    sortable: false,
    headerClass: "min-w-64 w-64",
    bodyClass: "h-12",
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

const tagMap = computed(() => {
  const map = new Map<string, string>();
  for (const tag of vocabularyStore.vocabularyTags ?? []) {
    map.set(tag.vocabularyTagId ?? "", tag.vocabularyTag);
  }
  return map;
});

const rows = computed<HistoryRow[]>(() =>
  (vocabularyStore.quizHistories ?? []).map((h, i) => ({
    ...h,
    index: i + 1,
    answeredAtFormatted: formatToJST(h.answeredAt) ?? "",
    tagNames: (h.tagIds ?? []).map((id) => tagMap.value.get(id) ?? id),
    directionLabel: getDirectionLabel(
      h.direction as "FRONT_TO_BACK" | "BACK_TO_FRONT",
    ),
  })),
);
</script>
