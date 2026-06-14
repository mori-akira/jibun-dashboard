<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[
          { text: 'Cardbook', iconName: 'tabler:books', link: '/cardbook' },
          { text: cardbookName, iconName: 'tabler:book' },
        ]"
      />
      <div class="flex items-center gap-2 mr-4">
        <Button
          type="navigation"
          size="small"
          button-class="w-28"
          @click:button="() => navigateTo(`/cardbook/${cardbookId}/quiz`)"
        >
          <Icon name="tabler:cards" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Quiz</span>
        </Button>
      </div>
    </div>

    <!-- Card List -->
    <Panel wrapper-class="overflow-x-auto">
      <div class="flex justify-between items-center">
        <h3>
          <Icon name="tabler:list" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Cards</span>
        </h3>
        <div class="flex items-center gap-4 mr-4">
          <Button
            type="delete"
            size="small"
            button-class="w-36"
            :disabled="checkedIds.length === 0"
            @click:button="onDeleteSelected"
          >
            <Icon name="tabler:trash" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Delete All</span>
          </Button>
          <Button
            type="add"
            size="small"
            button-class="w-32"
            @click:button="showAddModal = true"
          >
            <Icon name="tabler:plus" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Add Card</span>
          </Button>
        </div>
      </div>
      <DataTable
        :rows="cardRows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        :init-sort-state="initSortState"
        wrapper-class="flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
      />
    </Panel>

    <!-- Add Card Modal -->
    <ModalWindow
      :show-modal="showAddModal"
      modal-box-class="w-3/5 max-h-[80vh] overflow-y-auto"
      @close="onCloseAddModal"
    >
      <div class="flex justify-end">
        <IconButton type="cancel" icon-class="w-6 h-6" @click:button="onCloseAddModal" />
      </div>
      <TextBox
        v-model="newFront"
        label="Term"
        required
        wrapper-class="mt-4 w-full justify-center"
        label-class="w-40 ml-4 font-cursive"
        input-wrapper-class="w-1/2"
      />
      <TextArea
        v-model="newBack"
        label="Description"
        :rows="4"
        wrapper-class="mt-4 w-full justify-center items-start"
        label-class="w-40 ml-4 font-cursive mt-1"
        input-wrapper-class="w-1/2"
      />
      <Button
        type="action"
        wrapper-class="flex justify-center mt-8 mb-4"
        :disabled="!newFront.trim() || isLoading"
        @click:button="onAddCard"
      >
        <Icon name="tabler:plus" class="adjust-icon-4" />
        <span class="ml-2">Add</span>
      </Button>
    </ModalWindow>

    <!-- Edit Card Modal -->
    <ModalWindow
      :show-modal="editCard !== null"
      modal-box-class="w-3/5 max-h-[80vh] overflow-y-auto"
      @close="onCloseEditCardModal"
    >
      <div class="flex justify-end">
        <IconButton type="cancel" icon-class="w-6 h-6" @click:button="onCloseEditCardModal" />
      </div>
      <TextBox
        v-model="editFront"
        label="Term"
        required
        wrapper-class="mt-4 w-full justify-center"
        label-class="w-40 ml-4 font-cursive"
        input-wrapper-class="w-1/2"
      />
      <TextArea
        v-model="editBack"
        label="Description"
        :rows="4"
        wrapper-class="mt-4 w-full justify-center items-start"
        label-class="w-40 ml-4 font-cursive mt-1"
        input-wrapper-class="w-1/2"
      />
      <div class="mt-8 mb-4 flex justify-center">
        <Button
          type="action"
          size="small"
          button-class="w-28"
          :disabled="!editFront.trim() || isLoading"
          @click:button="onUpdateCard"
        >
          <Icon name="tabler:database-share" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Save</span>
        </Button>
      </div>
    </ModalWindow>

    <!-- Confirm Dialog -->
    <Dialog
      :show-dialog="showConfirmDialog"
      type="confirm"
      :message="confirmDialogMessage"
      button-type="yesNo"
      @click:yes="onConfirmYes"
      @click:no="onConfirmNo"
      @close="onConfirmNo"
    />
  </div>
</template>

<script setup lang="ts">
import type { CardbookCard } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import IconButton from "~/components/common/IconButton.vue";
import TextBox from "~/components/common/TextBox.vue";
import TextArea from "~/components/common/TextArea.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useCommonStore } from "~/stores/common";
import { useCardbookStore } from "~/stores/cardbook";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { useConfirmDialog } from "~/composables/common/useDialog";
import { getErrorMessage } from "~/utils/error";

const route = useRoute();
const cardbookId = route.params.cardbookId as string;

const commonStore = useCommonStore();
const cardbookStore = useCardbookStore();
const { isLoading, withLoading } = useLoadingQueue();
const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();

onMounted(async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        cardbookStore.fetchCardbooks(),
        cardbookStore.fetchCardbookCards(cardbookId),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});

const cardbookName = computed(
  () =>
    cardbookStore.cardbooks?.find((c) => c.cardbookId === cardbookId)?.name ??
    cardbookId,
);

// Card table
type CardRow = CardbookCard & {
  index: number;
  actions: null;
} & Record<string, unknown>;

const checkedIds = ref<string[]>([]);

const columnDefs: ColumnDef<CardRow>[] = [
  {
    checkable: true,
    checkStatusFunction: (row: CardRow) =>
      checkedIds.value.includes(row.cardId ?? "") ? "on" : "off",
    headerCheckable: true,
    headerCheckStatusFunction: (rows: CardRow[]) =>
      checkedIds.value.length === 0
        ? "off"
        : checkedIds.value.length < rows.length
          ? "neutral"
          : "on",
    headerClass: "w-8",
    bodyClass: "text-center h-12",
    onChangeCheck: (_, row: CardRow) => {
      if (!row.cardId) return;
      if (checkedIds.value.includes(row.cardId)) {
        checkedIds.value = checkedIds.value.filter((id) => id !== row.cardId);
      } else {
        checkedIds.value.push(row.cardId);
      }
    },
    onChangeHeaderCheck: (_, rows: CardRow[]) => {
      if (checkedIds.value.length < rows.length) {
        rows
          .map((r) => r.cardId)
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
    sortable: false,
    headerClass: "w-8",
    bodyClass: "h-12 text-center",
  },
  {
    field: "front",
    header: "Term",
    sortable: true,
    headerClass: "min-w-64 w-80",
    bodyClass: "h-12 truncate",
  },
  {
    field: "back",
    header: "Description",
    sortable: true,
    headerClass: "min-w-64 w-96",
    bodyClass: "h-12 truncate",
  },
  {
    header: "Action",
    actionButtons: ["edit"],
    onClickEdit: (row: CardRow) => {
      const card = cardbookStore.cardbookCards?.find(
        (c) => c.cardId === row.cardId,
      );
      if (!card) return;
      editCard.value = card;
      editFront.value = card.front;
      editBack.value = card.back ?? "";
    },
    headerClass: "w-20",
    bodyClass: "h-12",
    iconClass: "w-5 h-5 text-indigo-700 translate-y-1",
  },
];

const initSortState: SortDef<CardRow> = {
  column: "index",
  direction: "asc",
};

const cardRows = computed<CardRow[]>(() =>
  (cardbookStore.cardbookCards ?? []).map((c, i) => ({
    ...c,
    index: i + 1,
    actions: null,
  })),
);

// Add card
const showAddModal = ref(false);
const newFront = ref("");
const newBack = ref("");

function onCloseAddModal() {
  showAddModal.value = false;
  newFront.value = "";
  newBack.value = "";
}

async function onAddCard() {
  await withLoading(async () => {
    try {
      await cardbookStore.postCardbookCard(cardbookId, {
        front: newFront.value.trim(),
        back: newBack.value.trim() || undefined,
      });
      await cardbookStore.fetchCardbookCards(cardbookId);
      onCloseAddModal();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}

// Edit card
const editCard = ref<CardbookCard | null>(null);
const editFront = ref("");
const editBack = ref("");

function onCloseEditCardModal() {
  editCard.value = null;
  editFront.value = "";
  editBack.value = "";
}

async function onUpdateCard() {
  if (!editCard.value) return;
  await withLoading(async () => {
    try {
      await cardbookStore.putCardbookCard(
        cardbookId,
        editCard.value!.cardId!,
        {
          front: editFront.value.trim(),
          back: editBack.value.trim() || undefined,
        },
      );
      await cardbookStore.fetchCardbookCards(cardbookId);
      onCloseEditCardModal();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}

// Delete selected cards
async function onDeleteSelected() {
  const confirmed = await openConfirmDialog(
    "Confirm deletion of all checked cards?",
  );
  if (!confirmed) return;
  await withLoading(async () => {
    try {
      await Promise.all(
        checkedIds.value.map((cardId) =>
          cardbookStore.deleteCardbookCard(cardbookId, cardId),
        ),
      );
      checkedIds.value = [];
      await cardbookStore.fetchCardbookCards(cardbookId);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}
</script>
