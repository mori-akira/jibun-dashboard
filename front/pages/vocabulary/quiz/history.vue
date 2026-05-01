<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Vocabulary', iconName: 'tabler:book', link: '/vocabulary' },
        { text: 'Quiz', iconName: 'tabler:cards', link: '/vocabulary/quiz' },
        { text: 'Quiz History', iconName: 'tabler:history' },
      ]"
    />

    <Panel wrapper-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:list" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">History</span>
      </h3>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        :init-sort-state="initSortState"
        wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
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
  </div>
</template>

<script setup lang="ts">
import type { VocabularyQuizHistory } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { formatToJST } from "~/utils/date";
import { getDirectionLabel } from "~/utils/vocabularyQuiz";

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

onMounted(async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        vocabularyStore.fetchQuizHistories(),
        vocabularyStore.fetchVocabularyTags(),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});

type HistoryRow = VocabularyQuizHistory & {
  answeredAtFormatted: string;
  tagNames: string[];
  directionLabel: string;
} & Record<string, unknown>;

const columnDefs: ColumnDef<HistoryRow>[] = [
  {
    field: "answeredAtFormatted",
    header: "Answered At",
    sortable: true,
    headerClass: "w-32 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "tagNames",
    header: "Tags",
    sortable: false,
    headerClass: "w-48",
    bodyClass: "h-12",
  },
  {
    field: "questionCount",
    header: "Questions",
    sortable: true,
    headerClass: "w-20 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "directionLabel",
    header: "Direction",
    sortable: true,
    headerClass: "w-36 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "correctCount",
    header: "Correct",
    sortable: true,
    headerClass: "w-20 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "incorrectCount",
    header: "Incorrect",
    sortable: true,
    headerClass: "w-20 text-center",
    bodyClass: "h-12 text-center",
  },
];

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
