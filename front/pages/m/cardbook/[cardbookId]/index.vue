<template>
  <div>
    <div class="flex items-center justify-between mr-2">
      <Breadcrumb
        :items="[
          { text: 'Cardbook', iconName: 'tabler:books', link: '/m/cardbook' },
          { text: breadcrumbName, iconName: 'tabler:book' },
        ]"
      />
    </div>
    <div class="flex justify-end items-center gap-2 mt-2 mr-2">
      <Button
        type="navigation"
        size="small"
        html-type="button"
        button-class="w-28"
        @click:button="() => navigateTo(`/m/cardbook/${cardbookId}/quiz`)"
      >
        <Icon name="tabler:cards" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Quiz</span>
      </Button>
      <Button
        type="add"
        size="small"
        html-type="button"
        @click:button="showAddModal = true"
      >
        <Icon name="tabler:plus" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Add</span>
      </Button>
    </div>

    <div class="flex-1 w-full mt-4">
      <Panel wrapper-class="w-full ml-2 overflow-x-auto">
        <h3>
          <Icon name="tabler:list" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Cards</span>
        </h3>
        <DataTable
          :rows="cardRows"
          :column-defs="columnDefs"
          :is-loading="isLoading"
          :init-sort-state="initSortState"
          row-clickable
          wrapper-class="flex justify-center mt-4 mx-1"
          header-class="font-cursive h-8 bg-gray-800 text-white text-[0.9rem]"
          @click:row="onClickRow"
        />
      </Panel>
    </div>

    <!-- Detail Modal -->
    <ModalWindow
      :show-modal="selectedCard !== null"
      modal-box-class="overflow-x-auto"
      @close="selectedCard = null"
    >
      <InformationForm
        :item="selectedCard"
        :item-defs="itemDefs"
        wrapper-class="flex flex-col items-center w-[80vw]"
        label-class="bg-gray-800 text-white w-28 font-cursive"
        item-class="bg-gray-200 flex-1"
      />
    </ModalWindow>

    <!-- Add Card Modal -->
    <ModalWindow
      :show-modal="showAddModal"
      modal-box-class="w-[85vw]"
      @close="onCloseAddModal"
    >
      <div class="flex justify-end">
        <IconButton
          type="cancel"
          icon-class="w-6 h-6"
          @click:button="onCloseAddModal"
        />
      </div>
      <h3 class="font-cursive font-bold text-center mb-2">Add Card</h3>
      <TextBox
        v-model="newFront"
        label="Term"
        required
        wrapper-class="mt-4 w-full justify-center"
        label-class="ml-2 font-cursive"
        input-wrapper-class="flex-1 mr-4"
      />
      <Button
        type="action"
        wrapper-class="flex justify-center mt-6 mb-2"
        :disabled="!newFront.trim() || isLoading"
        @click:button="onAddCard"
      >
        <Icon name="tabler:plus" class="adjust-icon-4" />
        <span class="ml-2">Add</span>
      </Button>
    </ModalWindow>
  </div>
</template>

<script setup lang="ts">
import type { CardbookCard } from "~/generated/api/client/api";
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
import { useCommonStore } from "~/stores/common";
import { useCardbookStore } from "~/stores/cardbook";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";

definePageMeta({ layout: "mobile" });

const route = useRoute();
const cardbookId = route.params.cardbookId as string;

const commonStore = useCommonStore();
const cardbookStore = useCardbookStore();
const { isLoading, withLoading } = useLoadingQueue();

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
const breadcrumbName = computed(() =>
  cardbookName.value.length > 8
    ? cardbookName.value.slice(0, 8) + "…"
    : cardbookName.value,
);

type CardRow = CardbookCard & { index: number } & Record<string, unknown>;

const columnDefs: ColumnDef<CardRow>[] = [
  {
    field: "index",
    header: "",
    sortable: false,
    headerClass: "w-8",
    bodyClass: "h-12 text-center text-[0.9rem]",
  },
  {
    field: "front",
    header: "Term",
    sortable: true,
    headerClass: "min-w-48 w-full",
    bodyClass: "h-12 truncate text-[0.9rem]",
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
  })),
);

// Detail modal
const itemDefs: ItemDef[] = [
  { field: "front", label: "Term", skipIfNull: true },
  {
    field: "back",
    label: "Description",
    skipIfNull: true,
    itemType: "multiline",
  },
];
const selectedCard = ref<CardRow | null>(null);
function onClickRow(row: CardRow) {
  selectedCard.value = row;
}

// Add modal
const showAddModal = ref(false);
const newFront = ref("");

function onCloseAddModal() {
  showAddModal.value = false;
  newFront.value = "";
}

async function onAddCard() {
  await withLoading(async () => {
    try {
      await cardbookStore.postCardbookCard(cardbookId, {
        front: newFront.value.trim(),
      });
      await cardbookStore.fetchCardbookCards(cardbookId);
      onCloseAddModal();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}
</script>
