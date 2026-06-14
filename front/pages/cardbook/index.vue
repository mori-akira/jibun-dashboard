<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb :items="[{ text: 'Cardbook', iconName: 'tabler:books' }]" />
      <div class="flex items-center mr-4">
        <Button
          type="add"
          size="small"
          button-class="w-36"
          @click:button="showCreateModal = true"
        >
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">New Book</span>
        </Button>
      </div>
    </div>

    <Panel wrapper-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:books" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Cardbooks</span>
      </h3>
      <DataTable
        :rows="rows"
        :column-defs="columnDefs"
        :is-loading="isLoading"
        :init-sort-state="initSortState"
        wrapper-class="flex justify-center mt-4 ml-10 mr-10"
        header-class="font-cursive h-8 bg-gray-800 text-white"
        row-clickable
        @click:row="(row) => navigateTo(`/cardbook/${row.cardbookId}`)"
      >
        <template #cell-actions="{ row }">
          <div class="flex gap-3 justify-center items-center" @click.stop>
            <Icon
              name="tabler:cards"
              class="text-xl text-indigo-700 hover:cursor-pointer hover:text-indigo-500"
              title="Quiz"
              @click="navigateTo(`/cardbook/${row.cardbookId}/quiz`)"
            />
            <Icon
              name="tabler:edit"
              class="text-xl text-indigo-700 hover:cursor-pointer hover:text-indigo-500"
              title="Edit Name"
              @click="openEditModal(row)"
            />
            <Icon
              name="tabler:trash"
              class="text-xl text-red-500 hover:cursor-pointer hover:text-red-700"
              title="Delete"
              @click="onDeleteCardbook(row)"
            />
          </div>
        </template>
      </DataTable>
    </Panel>

    <!-- Create Modal -->
    <Dialog
      :show-dialog="showCreateModal"
      type="input"
      message="New Cardbook"
      button-type="okCancel"
      input-wrapper-class="w-80"
      @click:ok="onCreate"
      @click:cancel="onCloseCreateModal"
      @close="onCloseCreateModal"
    />

    <!-- Edit Name Modal -->
    <Dialog
      :show-dialog="editTarget !== null"
      type="input"
      message="Edit Cardbook Name"
      button-type="okCancel"
      input-wrapper-class="w-80"
      :initial-value="editTarget?.name ?? ''"
      @click:ok="onUpdateName"
      @click:cancel="onCloseEditModal"
      @close="onCloseEditModal"
    />
  </div>
</template>

<script setup lang="ts">
import type { Cardbook } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useCommonStore } from "~/stores/common";
import { useCardbookStore } from "~/stores/cardbook";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { formatToJST } from "~/utils/date";

const commonStore = useCommonStore();
const cardbookStore = useCardbookStore();
const { isLoading, withLoading } = useLoadingQueue();

const lastAnsweredMap = computed(() => {
  const map = new Map<string, string>();
  for (const h of cardbookStore.quizHistories ?? []) {
    if (!h.cardbookId || !h.answeredAt) continue;
    const existing = map.get(h.cardbookId);
    if (!existing || h.answeredAt > existing) {
      map.set(h.cardbookId, h.answeredAt);
    }
  }
  return map;
});

onMounted(async () => {
  await withLoading(async () => {
    try {
      await cardbookStore.fetchCardbooks();
      const ids = (cardbookStore.cardbooks ?? [])
        .map((b) => b.cardbookId)
        .filter((id): id is string => !!id);
      await Promise.all([
        cardbookStore.fetchQuizHistories(),
        cardbookStore.fetchCardCountMap(ids),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});

type CardbookRow = Cardbook & {
  index: number;
  cardCount: number | string;
  lastAnsweredAt: string;
  actions: null;
} & Record<string, unknown>;

const columnDefs: ColumnDef<CardbookRow>[] = [
  {
    field: "index",
    header: "",
    sortable: false,
    headerClass: "w-8",
    bodyClass: "h-12 text-center",
  },
  {
    field: "name",
    header: "Name",
    sortable: true,
    headerClass: "min-w-64 w-96",
    bodyClass: "h-12 truncate",
  },
  {
    field: "cardCount",
    header: "Cards",
    sortable: true,
    headerClass: "w-20 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "lastAnsweredAt",
    header: "Last Answered",
    sortable: true,
    headerClass: "min-w-48 w-52 text-center",
    bodyClass: "h-12 text-center",
  },
  {
    field: "actions",
    header: "",
    sortable: false,
    headerClass: "w-36",
    bodyClass: "h-12",
  },
];

const initSortState: SortDef<CardbookRow> = {
  column: "index",
  direction: "asc",
};

const rows = computed<CardbookRow[]>(() =>
  (cardbookStore.cardbooks ?? []).map((c, i) => ({
    ...c,
    index: i + 1,
    cardCount: cardbookStore.cardCountMap.get(c.cardbookId!) ?? "-",
    lastAnsweredAt:
      formatToJST(lastAnsweredMap.value.get(c.cardbookId!)) ?? "-",
    actions: null,
  })),
);

// Create
const showCreateModal = ref(false);

function onCloseCreateModal() {
  showCreateModal.value = false;
}

async function onCreate(name?: string) {
  if (!name?.trim()) return;
  await withLoading(async () => {
    try {
      const id = await cardbookStore.postCardbook({ name: name.trim() });
      onCloseCreateModal();
      if (id) await navigateTo(`/cardbook/${id}`);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}

// Edit Name
const editTarget = ref<CardbookRow | null>(null);

function openEditModal(row: CardbookRow) {
  editTarget.value = row;
}

function onCloseEditModal() {
  editTarget.value = null;
}

async function onUpdateName(name?: string) {
  if (!editTarget.value?.cardbookId || !name?.trim()) return;
  await withLoading(async () => {
    try {
      await cardbookStore.putCardbook(editTarget.value!.cardbookId!, {
        name: name.trim(),
      });
      await cardbookStore.fetchCardbooks();
      onCloseEditModal();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}

// Delete
async function onDeleteCardbook(row: CardbookRow) {
  if (!row.cardbookId) return;
  await withLoading(async () => {
    try {
      await cardbookStore.deleteCardbook(row.cardbookId!);
      await cardbookStore.fetchCardbooks();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}
</script>
