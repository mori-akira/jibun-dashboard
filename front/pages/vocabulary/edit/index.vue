<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Vocabulary', iconName: 'tabler:book', link: '/vocabulary' },
        { text: 'Edit', iconName: 'tabler:database-edit' },
      ]"
    />

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
      <div class="flex justify-between items-center mt-4">
        <Button
          type="delete"
          size="small"
          wrapper-class="mx-8"
          :disabled="checkedIds.length === 0"
          @click:button="onDeleteAll"
        >
          <Icon name="tabler:trash" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Delete All</span>
        </Button>
        <Button
          type="add"
          size="small"
          wrapper-class="mr-8"
          @click:button="onAddNewOne"
        >
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Add New One</span>
        </Button>
      </div>
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
      :show-modal="editTarget !== undefined"
      modal-box-class="w-1/2 h-[80vh] flex-col items-center overflow-y-auto"
      @close="onCloseEditModal"
    >
      <FormEditor
        :target-vocabulary="editTarget"
        :vocabulary-tags="vocabularyStore.vocabularyTags ?? []"
        @submit="onSubmitEdit"
        @close-modal="onCloseEditModal"
        @added-tag="vocabularyStore.fetchVocabularyTags()"
      />
    </ModalWindow>

    <Dialog
      :show-dialog="showInfoDialog"
      type="info"
      :message="infoDialogMessage"
      button-type="ok"
      @click:ok="onInfoOk"
      @close="onInfoOk"
    />

    <Dialog
      :show-dialog="showConfirmDialog"
      type="confirm"
      :message="confirmDialogMessage"
      button-type="yesNo"
      @click:yes="onConfirmYes"
      @click:no="onConfirmNo"
      @close="onConfirmNo"
    />

    <Dialog
      :show-dialog="showErrorDialog"
      type="error"
      :message="errorDialogMessage"
      button-type="ok"
      @click:ok="onErrorOk"
      @close="onErrorOk"
    />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";

import type { Vocabulary, VocabularyRequest } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import Dialog from "~/components/common/Dialog.vue";
import SearchConditionForm from "~/components/vocabulary/SearchConditionForm.vue";
import FormEditor from "~/components/vocabulary/edit/FormEditor.vue";
import {
  useInfoDialog,
  useConfirmDialog,
  useErrorDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { withErrorHandling } from "~/utils/api-call";

const { t } = useI18n();
const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();

const { showInfoDialog, infoDialogMessage, openInfoDialog, onInfoOk } =
  useInfoDialog();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();
const { showErrorDialog, errorDialogMessage, openErrorDialog, onErrorOk } =
  useErrorDialog();

const { isLoading, withLoading } = useLoadingQueue();
const vocabularyName = ref("");
const description = ref("");
const selectedTagIds = ref<string[]>([]);

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

const checkedIds = ref<string[]>([]);

const columnDefs: ColumnDef<VocabularyWithIndex>[] = [
  {
    checkable: true,
    checkStatusFunction: (row: VocabularyWithIndex) =>
      checkedIds.value.includes(row.vocabularyId ?? "") ? "on" : "off",
    headerCheckable: true,
    headerCheckStatusFunction: (rows: VocabularyWithIndex[]) =>
      checkedIds.value.length === 0
        ? "off"
        : checkedIds.value.length < rows.length
        ? "neutral"
        : "on",
    headerClass: "w-8",
    bodyClass: "text-center h-12",
    onChangeCheck: (_, row: VocabularyWithIndex) => {
      if (!row.vocabularyId) return;
      if (checkedIds.value.includes(row.vocabularyId)) {
        checkedIds.value = checkedIds.value.filter(
          (id) => id !== row.vocabularyId,
        );
      } else {
        checkedIds.value.push(row.vocabularyId);
      }
    },
    onChangeHeaderCheck: (_, rows: VocabularyWithIndex[]) => {
      if (checkedIds.value.length < rows.length) {
        rows
          .map((r) => r.vocabularyId)
          .filter((id): id is string => !!id)
          .forEach(
            (id) => !checkedIds.value.includes(id) && checkedIds.value.push(id),
          );
      } else {
        checkedIds.value = [];
      }
    },
  },
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
  {
    header: "Action",
    actionButtons: ["edit"],
    onClickEdit: (row: VocabularyWithIndex) => {
      const target = vocabularyStore.vocabularies?.find(
        (v) => v.vocabularyId === row.vocabularyId,
      );
      editTarget.value = target ? { ...target } : undefined;
    },
    headerClass: "w-20",
    bodyClass: "h-12",
    iconClass: "w-5 h-5 text-indigo-700 translate-y-1",
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

const editTarget = ref<Vocabulary | undefined>(undefined);

const onAddNewOne = () => {
  editTarget.value = { name: "" };
};

const onSubmitEdit = async (value: VocabularyRequest) => {
  const result = await withErrorHandling(async () => {
    await vocabularyStore.putVocabulary(editTarget.value?.vocabularyId, {
      name: value.name,
      description: value.description || undefined,
      tagIds: value.tagIds,
    });
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    editTarget.value = undefined;
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchData();
  }
};

const onCloseEditModal = async () => {
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      t("message.confirm.checkUnsavedChanges"),
    );
    if (!confirmed) return;
  }
  editTarget.value = undefined;
  commonStore.setHasUnsavedChange(false);
};

const onDeleteAll = async () => {
  const confirmed = await openConfirmDialog(
    "Confirm deletion of all checked vocabularies?",
  );
  if (!confirmed) return;
  const id = commonStore.addLoadingQueue();
  try {
    await Promise.all(
      checkedIds.value.map((vid) => vocabularyStore.deleteVocabulary(vid)),
    );
    checkedIds.value = [];
    commonStore.deleteLoadingQueue(id);
    await openInfoDialog(t("message.info.completeSuccessfully"));
  } catch (err) {
    console.error(err);
    await openErrorDialog(t("message.error.somethingCloudNotDeleted"));
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
  await fetchData();
};
</script>
