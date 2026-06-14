<template>
  <div>
    <div class="flex items-center justify-between mr-2">
      <Breadcrumb :items="[{ text: 'Cardbook', iconName: 'tabler:books' }]" />
    </div>
    <div class="flex justify-end items-center gap-2 mt-2 mr-2">
      <Button
        type="add"
        size="small"
        html-type="button"
        @click:button="openAddModal"
      >
        <Icon name="tabler:plus" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">New Book</span>
      </Button>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel wrapper-class="w-full ml-2 overflow-x-auto">
        <h3>
          <Icon name="tabler:books" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Cardbooks</span>
        </h3>
        <DataTable
          :rows="rows"
          :column-defs="columnDefs"
          :is-loading="isLoading"
          :init-sort-state="initSortState"
          row-clickable
          wrapper-class="flex justify-center mt-4 mx-1"
          header-class="font-cursive h-8 bg-gray-800 text-white text-[0.9rem]"
          @click:row="(row) => navigateTo(`/m/cardbook/${row.cardbookId}`)"
        />
      </Panel>
    </div>

    <!-- Add Modal -->
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
        <h3 class="font-cursive font-bold text-center mb-2">New Cardbook</h3>
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
import type { Cardbook } from "~/generated/api/client/api";
import { schemas } from "~/generated/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import Button from "~/components/common/Button.vue";
import IconButton from "~/components/common/IconButton.vue";
import TextBox from "~/components/common/TextBox.vue";
import { useCommonStore } from "~/stores/common";
import { useCardbookStore } from "~/stores/cardbook";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

definePageMeta({ layout: "mobile" });

const commonStore = useCommonStore();
const cardbookStore = useCardbookStore();
const { isLoading, withLoading } = useLoadingQueue();

onMounted(async () => {
  await withLoading(async () => {
    try {
      await cardbookStore.fetchCardbooks();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});

type CardbookRow = Cardbook & { index: number } & Record<string, unknown>;

const columnDefs: ColumnDef<CardbookRow>[] = [
  {
    field: "index",
    header: "",
    sortable: false,
    headerClass: "w-8",
    bodyClass: "h-12 text-center text-[0.9rem]",
  },
  {
    field: "name",
    header: "Name",
    sortable: true,
    headerClass: "min-w-48 w-full",
    bodyClass: "h-12 truncate text-[0.9rem]",
  },
];

const initSortState: SortDef<CardbookRow> = {
  column: "index",
  direction: "asc",
};

const rows = computed<CardbookRow[]>(() =>
  (cardbookStore.cardbooks ?? []).map((c, i) => ({ ...c, index: i + 1 })),
);

// Add modal
const showAddModal = ref(false);
const addValidationRules = {
  name: zodToVeeRules(schemas.CardbookBase.shape.name),
};

function openAddModal() {
  showAddModal.value = true;
}

function onCloseAddModal() {
  showAddModal.value = false;
}

const onAddSubmit: SubmissionHandler<GenericObject> = async (values) => {
  await withLoading(async () => {
    try {
      const id = await cardbookStore.postCardbook({
        name: values.name as string,
      });
      onCloseAddModal();
      if (id) await navigateTo(`/m/cardbook/${id}`);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
};
</script>
