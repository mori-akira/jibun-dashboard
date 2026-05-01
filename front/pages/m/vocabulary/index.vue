<template>
  <div>
    <div class="flex items-center justify-between mr-2">
      <Breadcrumb :items="[{ text: 'Vocabulary', iconName: 'tabler:book' }]" />
    </div>
    <div class="flex justify-end items-center gap-2 mt-2 mr-2">
      <Button
        type="navigation"
        size="small"
        html-type="button"
        button-class="w-28"
        @click:button="() => navigateTo('/m/vocabulary/quiz')"
      >
        <Icon name="tabler:cards" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Quiz</span>
      </Button>
      <Button
        type="add"
        size="small"
        html-type="button"
        @click:button="openAddModal"
      >
        <Icon name="tabler:plus" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Add</span>
      </Button>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel wrapper-class="w-full ml-2">
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
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel wrapper-class="w-full ml-2 overflow-x-auto">
        <h3>
          <Icon name="tabler:list" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Result</span>
        </h3>
        <DataTable
          :rows="rows"
          :column-defs="columnDefs"
          :is-loading="isLoading"
          :init-sort-state="initSortState"
          row-clickable
          wrapper-class="flex justify-center mt-4 mx-1"
          header-class="font-cursive h-8 bg-gray-800 text-white text-[0.9rem]"
          @click:row="onClickRow"
        >
          <template #cell-tagNames="{ row }">
            <div class="flex flex-nowrap gap-1 overflow-hidden">
              <span
                v-for="badge in (row.tagBadges as TagBadge[])"
                :key="badge.name"
                :class="[
                  'shrink-0 py-[0.2rem] px-2 text-[0.7rem] text-white rounded-lg',
                  badge.selected ? 'bg-[#bb88ff]' : 'bg-[#888]',
                ]"
                >{{ badge.name }}</span
              >
            </div>
          </template>
        </DataTable>
      </Panel>
    </div>

    <ModalWindow
      :show-modal="selectedVocabulary !== null"
      modal-box-class="overflow-x-auto"
      @close="onCloseModal"
    >
      <InformationForm
        :item="selectedVocabulary"
        :item-defs="itemDefs"
        wrapper-class="flex flex-col items-center w-[80vw]"
        label-class="bg-gray-800 text-white w-28 font-cursive"
        item-class="bg-gray-200 flex-1"
      />
    </ModalWindow>

    <ModalWindow
      :show-modal="showAddModal"
      modal-box-class="w-[85vw]"
      @close="onCloseAddModal"
    >
      <Form v-slot="{ meta, handleSubmit }">
        <div class="flex justify-end">
          <IconButton
            type="cancel"
            icon-class="w-6 h-6"
            @click:button="onCloseAddModal"
          />
        </div>
        <h3 class="font-cursive font-bold text-center mb-2">Add Vocabulary</h3>
        <Field
          v-slot="{ field, errorMessage }"
          name="name"
          :rules="addValidationRules.name"
        >
          <TextBox
            label="Name"
            v-bind="field"
            :error-message="errorMessage"
            required
            wrapper-class="mt-4 w-full justify-center"
            label-class="ml-2 font-cursive"
            input-wrapper-class="flex-1 mr-8"
            @blur:event="field.onBlur"
          />
        </Field>
        <Button
          :disabled="!meta.valid"
          type="action"
          wrapper-class="flex justify-center mt-6 mb-2"
          @click:button="handleSubmit(onAddSubmit)"
        >
          <Icon name="tabler:database-share" class="adjust-icon-4" />
          <span class="ml-2">Add</span>
        </Button>
      </Form>
    </ModalWindow>
  </div>
</template>

<script setup lang="ts">
import { Form, Field } from "vee-validate";
import type { SubmissionHandler, GenericObject } from "vee-validate";
import type { Vocabulary } from "~/generated/api/client/api";
import { schemas } from "~/generated/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import InformationForm from "~/components/common/InformationForm.vue";
import type { ItemDef } from "~/components/common/InformationForm.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import TextBox from "~/components/common/TextBox.vue";
import SearchConditionForm from "~/components/vocabulary/SearchConditionForm.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

definePageMeta({
  layout: "mobile",
});

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

// --- Search conditions ---

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

// --- Data fetching ---

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

// --- Table ---

type TagBadge = { name: string; selected: boolean };
type VocabularyWithIndex = Vocabulary & {
  index: number;
  tagNames: string;
  tagBadges: TagBadge[];
  tagNameList: string[];
} & Record<string, unknown>;

const columnDefs: ColumnDef<VocabularyWithIndex>[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12 text-[0.9rem]",
  },
  {
    field: "name",
    header: "Name",
    sortable: true,
    headerClass: "w-64",
    bodyClass: "h-12 text-[0.9rem] truncate",
  },
  {
    field: "tagNames",
    header: "Tags",
    sortable: false,
    headerClass: "w-64",
    bodyClass: "h-12",
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
    tagNameList: Array.from(v.tags ?? []).map((t) => t.vocabularyTag),
  })),
);

const initSortState: SortDef<VocabularyWithIndex> = {
  column: "index",
  direction: "asc",
};

// --- Detail modal ---

const itemDefs: ItemDef[] = [
  {
    field: "name",
    label: "Name",
    skipIfNull: true,
  },
  {
    field: "description",
    label: "Description",
    skipIfNull: true,
    itemType: "multiline",
  },
  {
    field: "tagNameList",
    label: "Tags",
    skipIfNull: true,
    itemType: "badges",
  },
];
const selectedVocabulary = ref<VocabularyWithIndex | null>(null);
const onClickRow = (row: VocabularyWithIndex) => {
  selectedVocabulary.value = row;
};
const onCloseModal = () => (selectedVocabulary.value = null);

// --- Add modal ---

const showAddModal = ref(false);
const addValidationRules = {
  name: zodToVeeRules(schemas.VocabularyRequest.shape.name),
};
const openAddModal = () => {
  showAddModal.value = true;
};
const onCloseAddModal = () => {
  showAddModal.value = false;
};
const onAddSubmit: SubmissionHandler<GenericObject> = async (values) => {
  await withLoading(async () => {
    try {
      await vocabularyStore.postVocabulary({ name: values.name as string });
      showAddModal.value = false;
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
};
</script>
