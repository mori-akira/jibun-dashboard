<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Vocabulary', iconName: 'tabler:book', link: '/vocabulary' },
        { text: 'Tags', iconName: 'tabler:tags' },
      ]"
    />

    <Panel wrapper-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:tags" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Vocabulary Tags</span>
      </h3>

      <div class="flex justify-end gap-2 mt-4 mr-8">
        <Button type="action" size="small" @click:button="onReorderItems">
          <Icon name="tabler:menu-order" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Reorder Items</span>
        </Button>
        <Button type="add" size="small" @click:button="onAddNewOne">
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Add New One</span>
        </Button>
      </div>

      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        :init-sort-state="initSortState"
        wrapper-class="flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
      />
    </Panel>

    <!-- Edit modal -->
    <ModalWindow
      :show-modal="editTarget !== undefined"
      modal-box-class="w-96 flex-col items-center"
      @close="onCloseEditModal"
    >
      <div class="w-full p-4">
        <div class="flex justify-end mb-2">
          <IconButton
            type="cancel"
            icon-class="w-6 h-6"
            @click:button="onCloseEditModal"
          />
        </div>
        <Form v-slot="{ meta, handleSubmit }">
          <Field
            v-slot="{ field, errorMessage }"
            name="vocabularyTag"
            :rules="tagValidationRules"
            :value="editTarget?.vocabularyTag ?? ''"
          >
            <TextBox
              label="Tag"
              v-bind="field"
              :error-message="errorMessage"
              required
              wrapper-class="mt-4 w-full justify-center"
              label-class="w-24 ml-4 font-cursive"
              input-wrapper-class="w-48"
              @blur:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Button
            :disabled="!meta.valid"
            type="action"
            wrapper-class="flex justify-center mt-6 mb-2"
            @click:button="handleSubmit(onSubmitTag)"
          >
            <Icon name="tabler:database-share" class="adjust-icon-4" />
            <span class="ml-2">Execute</span>
          </Button>
        </Form>
      </div>
    </ModalWindow>

    <!-- Reorder modal -->
    <ModalWindow
      :show-modal="reorderTarget !== undefined"
      modal-box-class="w-96 h-[80vh] flex-col items-center overflow-y-auto"
      @close="onCloseReorderModal"
    >
      <ReorderEditor
        :target-tags="reorderTarget"
        @submit="onSubmitReorder"
        @close-modal="onCloseReorderModal"
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
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import type {
  VocabularyTag,
  VocabularyTagBase,
} from "~/generated/api/client/api";
import { schemas } from "~/generated/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import TextBox from "~/components/common/TextBox.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import Dialog from "~/components/common/Dialog.vue";
import ReorderEditor from "~/components/vocabulary/edit/ReorderEditor.vue";
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
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

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

const fetchTags = async () => {
  await withLoading(async () => {
    try {
      await vocabularyStore.fetchVocabularyTags();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
};

onMounted(async () => await fetchTags());

type VocabularyTagWithIndex = VocabularyTag & { index: number } & Record<
    string,
    unknown
  >;

const columnDefs: ColumnDef<VocabularyTagWithIndex>[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12",
  },
  {
    field: "vocabularyTag",
    header: "Tag",
    sortable: true,
    headerClass: "w-64",
    bodyClass: "h-12",
  },
  {
    header: "Action",
    actionButtons: ["edit", "delete"],
    onClickEdit: (row: VocabularyTagWithIndex) => {
      const target = vocabularyStore.vocabularyTags?.find(
        (t) => t.vocabularyTagId === row.vocabularyTagId,
      );
      editTarget.value = target ? { ...target } : undefined;
    },
    onClickDelete: (row: VocabularyTagWithIndex) => {
      onDeleteOne(row.vocabularyTagId ?? "");
    },
    headerStyle: { width: "4rem", maxWidth: "4rem" },
    bodyClass: "h-12",
    iconClass: "w-5 h-5 text-indigo-700 translate-y-1",
  },
];

const rows = computed<VocabularyTagWithIndex[]>(() =>
  (vocabularyStore.vocabularyTags ?? []).map((t, i) => ({ ...t, index: i + 1 })),
);

const initSortState: SortDef<VocabularyTagWithIndex> = {
  column: "index",
  direction: "asc",
};

const tagValidationRules = zodToVeeRules(
  schemas.VocabularyTagBase.shape.vocabularyTag,
);

// ── Create / Edit ────────────────────────────────────────────────────────────

const editTarget = ref<VocabularyTag | undefined>(undefined);

const onAddNewOne = () => {
  editTarget.value = {
    vocabularyTag: "",
    order: (vocabularyStore.vocabularyTags?.length ?? 0) + 1,
  };
};

const onSubmitTag: SubmissionHandler<GenericObject> = async (values) => {
  const payload: VocabularyTagBase = {
    vocabularyTag: values.vocabularyTag as string,
    order: editTarget.value?.order ?? (vocabularyStore.vocabularyTags?.length ?? 0) + 1,
  };
  const result = await withErrorHandling(async () => {
    await vocabularyStore.putVocabularyTag(
      editTarget.value?.vocabularyTagId,
      payload,
    );
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    editTarget.value = undefined;
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchTags();
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

// ── Reorder ──────────────────────────────────────────────────────────────────

const reorderTarget = ref<VocabularyTag[] | undefined>(undefined);

const onReorderItems = () => {
  reorderTarget.value = (vocabularyStore.vocabularyTags ?? []).map((t) => ({ ...t }));
};

const onSubmitReorder = async (reordered: VocabularyTag[]) => {
  const original = new Map(
    (vocabularyStore.vocabularyTags ?? []).map((t) => [t.vocabularyTagId, t]),
  );
  const changed = reordered.filter(
    (t) => original.get(t.vocabularyTagId)?.order !== t.order,
  );
  const result = await withErrorHandling(async () => {
    await Promise.all(
      changed.map((t) => {
        const src = original.get(t.vocabularyTagId);
        if (!src) return;
        return vocabularyStore.putVocabularyTag(t.vocabularyTagId, {
          vocabularyTag: src.vocabularyTag,
          order: t.order,
        });
      }),
    );
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    reorderTarget.value = undefined;
    await openInfoDialog(t("message.info.completeSuccessfully"));
    await fetchTags();
  }
};

const onCloseReorderModal = async () => {
  if (commonStore.hasUnsavedChange) {
    const confirmed = await openConfirmDialog(
      t("message.confirm.checkUnsavedChanges"),
    );
    if (!confirmed) return;
  }
  reorderTarget.value = undefined;
  commonStore.setHasUnsavedChange(false);
};

// ── Delete ───────────────────────────────────────────────────────────────────

const onDeleteOne = async (vocabularyTagId: string) => {
  const confirmed = await openConfirmDialog("Confirm deletion of this tag?");
  if (!confirmed) return;
  const loadingId = commonStore.addLoadingQueue();
  try {
    await vocabularyStore.deleteVocabularyTag(vocabularyTagId);
    commonStore.deleteLoadingQueue(loadingId);
    await openInfoDialog(t("message.info.completeSuccessfully"));
  } catch (err) {
    console.error(err);
    await openErrorDialog(t("message.error.somethingCloudNotDeleted"));
  } finally {
    commonStore.deleteLoadingQueue(loadingId);
  }
  await fetchTags();
};
</script>
