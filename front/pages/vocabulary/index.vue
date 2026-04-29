<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb :items="[{ text: 'Vocabulary', iconName: 'tabler:book' }]" />
      <div class="flex items-center gap-2 mr-4">
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-38"
          @click:button="() => navigateTo('/vocabulary/tags')"
        >
          <Icon name="tabler:tags" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Manage Tags</span>
        </Button>
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-32"
          @click:button="() => navigateTo('/vocabulary/edit')"
        >
          <Icon name="tabler:database-edit" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Edit</span>
        </Button>
      </div>
    </div>

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
          <div class="flex flex-nowrap gap-1 overflow-hidden min-w-0">
            <span
              v-for="badge in (row.tagBadges as TagBadge[])"
              :key="badge.name"
              :class="[
                'shrink-0 py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg',
                badge.selected ? 'bg-[#bb88ff]' : 'bg-[#888]',
              ]"
              >{{ badge.name }}</span
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
  </div>
</template>

<script setup lang="ts">
import type { Vocabulary } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import SearchConditionForm from "~/components/vocabulary/SearchConditionForm.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { formatToJST } from "~/utils/date";

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

const route = useRoute();
const queryTagIds = route.query.tagIds;

const vocabularyName = ref("");
const description = ref("");
const selectedTagIds = ref<string[]>(
  queryTagIds
    ? Array.isArray(queryTagIds)
      ? (queryTagIds as string[])
      : [queryTagIds as string]
    : [],
);

const fetchData = async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        vocabularyStore.fetchVocabularies(
          vocabularyName.value,
          description.value,
          selectedTagIds.value,
        ),
        vocabularyStore.fetchVocabularyTags(),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
};

onMounted(async () => await fetchData());

watch(
  () => [vocabularyName, description, selectedTagIds],
  async () => {
    await withLoading(async () => {
      try {
        await vocabularyStore.fetchVocabularies(
          vocabularyName.value,
          description.value,
          selectedTagIds.value,
        );
      } catch (err) {
        console.error(err);
        commonStore.addErrorMessage(getErrorMessage(err));
      }
    });
  },
  { deep: true },
);

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
