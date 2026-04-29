<template>
  <div>
    <Breadcrumb :items="[{ text: 'Vocabulary', iconName: 'tabler:book' }]" />

    <div
      v-if="status === 'forbidden'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:lock" class="text-5xl" />
      <p>This share link does not include vocabulary data.</p>
    </div>

    <div
      v-else-if="status === 'gone'"
      class="flex flex-col items-center py-16 gap-2 text-gray-500"
    >
      <Icon name="tabler:clock-off" class="text-5xl" />
      <p>This share link has expired.</p>
    </div>

    <template v-else>
      <Panel>
        <h3>
          <Icon name="tabler:search" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Condition</span>
        </h3>
        <SearchConditionForm
          v-model:vocabulary-name="vocabularyName"
          v-model:description="description"
          v-model:selected-tag-ids="selectedTagIds"
          :vocabulary-tags="vocabularyStore.vocabularyTags ?? []"
        />
      </Panel>

      <Panel wrapper-class="overflow-x-auto">
        <h3>
          <Icon name="tabler:list" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Result</span>
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
          <template #cell-tagNames="{ row }">
            <div class="flex flex-nowrap items-center gap-1 overflow-hidden">
              <div class="flex flex-nowrap gap-1 overflow-hidden min-w-0">
                <span
                  v-for="badge in (row.tagBadges as TagBadge[]).slice(0, 3)"
                  :key="badge.name"
                  :class="[
                    'shrink-0 py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg',
                    badge.selected ? 'bg-[#bb88ff]' : 'bg-[#888]',
                  ]"
                  >{{ badge.name }}</span
                >
              </div>
              <span
                v-if="(row.tagBadges as TagBadge[]).length > 3"
                class="shrink-0 text-[0.8rem] text-gray-400"
                >...</span
              >
            </div>
          </template>
        </DataTable>
      </Panel>

      <ModalWindow
        :show-modal="selectedVocabulary !== null"
        @close="onCloseModal"
      >
        <InformationForm
          :item="selectedVocabulary"
          :item-defs="itemDefs"
          wrapper-class="flex flex-col items-center w-[80vw] max-w-3xl"
          label-class="bg-gray-800 text-white w-1/3 font-cursive"
          item-class="bg-gray-200 w-2/3"
        />
      </ModalWindow>
    </template>
  </div>
</template>

<script setup lang="ts">
import { AxiosError } from "axios";
import type { Ref } from "vue";
import type { Vocabulary } from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { formatToJST } from "~/utils/date";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import SearchConditionForm from "~/components/vocabulary/SearchConditionForm.vue";

definePageMeta({ layout: "share" });

const route = useRoute();
const token = route.params.token as string;
const { getShareApi } = useApiClient();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

const status = ref<"ok" | "forbidden" | "gone">("ok");
const allVocabularies = ref<Vocabulary[]>([]);
const shareStatus = inject<Ref<"ok" | "gone">>("shareStatus")!;
const forbiddenTypes = inject<Ref<string[]>>("forbiddenTypes")!;

onMounted(async () => {
  await withLoading(async () => {
    try {
      const [vocabularyRes, tagsRes] = await Promise.all([
        getShareApi().getShareVocabularies(token),
        getShareApi().getShareVocabularyTags(token),
      ]);
      allVocabularies.value = vocabularyRes.data;
      vocabularyStore.vocabularyTags = tagsRes.data;
      applyFilters();
    } catch (err) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 403) {
          status.value = "forbidden";
          forbiddenTypes.value = [...forbiddenTypes.value, "vocabulary"];
        } else if (err.response?.status === 410) status.value = "gone";
      }
    }
    if (status.value === "gone") shareStatus.value = "gone";
  });
});

const vocabularyName = ref("");
const description = ref("");
const selectedTagIds = ref<string[]>([]);

const applyFilters = () => {
  vocabularyStore.vocabularies = allVocabularies.value.filter((v) => {
    if (
      vocabularyName.value &&
      !v.name.toLowerCase().includes(vocabularyName.value.toLowerCase())
    )
      return false;
    if (
      description.value &&
      !v.description?.toLowerCase().includes(description.value.toLowerCase())
    )
      return false;
    if (
      selectedTagIds.value.length &&
      !selectedTagIds.value.some((tagId) =>
        Array.from(v.tags ?? []).some((t) => t.vocabularyTagId === tagId),
      )
    )
      return false;
    return true;
  });
};

watch([vocabularyName, description, selectedTagIds], applyFilters, {
  deep: true,
});

type TagBadge = { name: string; selected: boolean };
type VocabularyWithIndex = Vocabulary & {
  index: number;
  tagNames: string;
  tagBadges: TagBadge[];
} & Record<string, unknown>;

const columnDefs: ColumnDef<VocabularyWithIndex>[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12",
  },
  {
    field: "name",
    header: "Name",
    sortable: true,
    headerClass: "w-64",
    bodyClass: "h-12 truncate",
  },
  {
    field: "description",
    header: "Description",
    sortable: true,
    headerClass: "w-96",
    bodyClass: "h-12 truncate",
  },
  {
    field: "tagNames",
    header: "Tags",
    sortable: false,
    headerClass: "w-48",
    bodyClass: "h-12 text-sm text-gray-600",
  },
];

const rows = computed<VocabularyWithIndex[]>(() =>
  (vocabularyStore.vocabularies ?? []).map((v, i) => ({
    ...v,
    index: i + 1,
    tagNames: Array.from(v.tags ?? [])
      .map((t) => t.vocabularyTag)
      .join(", "),
    tagBadges: Array.from(v.tags ?? []).map((t) => ({
      name: t.vocabularyTag,
      selected: selectedTagIds.value.includes(t.vocabularyTagId ?? ""),
    })),
  })),
);

const initSortState: SortDef<VocabularyWithIndex> = {
  column: "index",
  direction: "asc",
};

const selectedVocabulary = ref<Vocabulary | null>(null);
const itemDefs: ItemDef[] = [
  { field: "vocabularyId", label: "Id", skipIfNull: true },
  { field: "name", label: "Name", skipIfNull: true },
  {
    field: "description",
    label: "Description",
    skipIfNull: true,
    itemType: "multiline",
    itemClass: "!whitespace-normal items-start",
  },
  {
    field: "tagNames",
    label: "Tags",
    skipIfNull: true,
    itemType: "badges",
    itemClass: "!whitespace-normal !overflow-visible items-start py-1",
  },
  { field: "createdDateTime", label: "Created", skipIfNull: true },
  { field: "updatedDateTime", label: "Updated", skipIfNull: true },
];

const onClickRow = (row: VocabularyWithIndex) => {
  const found = vocabularyStore.vocabularies?.find(
    (v) => v.vocabularyId === row.vocabularyId,
  );
  selectedVocabulary.value = found
    ? ({
        ...found,
        tagNames: Array.from(found.tags ?? []).map((t) => t.vocabularyTag),
        createdDateTime: formatToJST(found.createdDateTime),
        updatedDateTime: formatToJST(found.updatedDateTime),
      } as unknown as Vocabulary)
    : null;
};

const onCloseModal = () => (selectedVocabulary.value = null);
</script>
