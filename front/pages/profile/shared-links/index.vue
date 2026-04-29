<template>
  <div>
    <Breadcrumb :items="[{ text: 'Shared Links', iconName: 'tabler:link' }]" />

    <Panel wrapper-class="overflow-x-auto">
      <h3>
        <Icon name="tabler:list" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Links</span>
      </h3>
      <div class="flex justify-end mt-4">
        <Button
          type="add"
          size="small"
          wrapper-class="mr-8"
          @click:button="showCreateModal = true"
        >
          <Icon name="tabler:plus" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Create</span>
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
        <template #cell-shareUrl="{ row }">
          <a
            :href="(row.shareUrl as string)"
            target="_blank"
            rel="noopener noreferrer"
            class="text-blue-600 underline truncate block max-w-full"
            @click.stop
            >{{ row.shareUrl }}</a
          >
        </template>
        <template #cell-dataTypesBadges="{ row }">
          <div class="flex flex-nowrap gap-1 overflow-hidden min-w-0">
            <span
              v-for="type in (row.dataTypesBadges as string[])"
              :key="type"
              class="shrink-0 py-[0.2rem] px-2 text-[0.8rem] text-white rounded-lg bg-[#888]"
              >{{ type }}</span
            >
          </div>
        </template>
      </DataTable>
    </Panel>

    <ModalWindow
      :show-modal="showCreateModal"
      modal-box-class="w-128 flex-col items-center py-8 px-10"
      @close="onCloseCreateModal"
    >
      <h3 class="mb-6">
        <Icon name="tabler:link" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Create Shared Link</span>
      </h3>
      <div class="flex flex-col gap-5 w-full">
        <div class="flex">
          <p class="font-cursive mb-2 mr-4">
            Data Types <span class="text-red-500">*</span>
          </p>
          <div class="flex flex-col gap-2 ml-2">
            <div
              v-for="type in dataTypeOptions"
              :key="type.value"
              class="flex items-center gap-2 cursor-pointer"
              @click="toggleDataType(type.value)"
            >
              <CheckBox
                :status="newDataTypes.includes(type.value) ? 'on' : 'off'"
                type="onOff"
              />
              <span class="font-cursive capitalize">{{ type.label }}</span>
            </div>
          </div>
        </div>
        <DatePicker
          label="Expires At"
          :date="newExpiresAt"
          label-class="font-cursive w-24 mr-4"
          required
          @change:date="(v: string | undefined) => (newExpiresAt = v ?? '')"
        />
        <div class="flex justify-center gap-4 mt-2">
          <Button type="navigation" @click:button="onCloseCreateModal">
            <span class="font-bold">Cancel</span>
          </Button>
          <Button
            type="add"
            :disabled="newDataTypes.length === 0 || !newExpiresAt"
            @click:button="onCreateLink"
          >
            <Icon name="tabler:link" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Create</span>
          </Button>
        </div>
      </div>
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

import type { SharedLink } from "~/generated/api/client/api";
import { SharedLinkDataTypesEnum } from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import CheckBox from "~/components/common/CheckBox.vue";
import DatePicker from "~/components/common/DatePicker.vue";
import DataTable from "~/components/common/DataTable.vue";
import type { ColumnDef, SortDef } from "~/components/common/DataTable.vue";
import ModalWindow from "~/components/common/ModalWindow.vue";
import Dialog from "~/components/common/Dialog.vue";
import {
  useInfoDialog,
  useConfirmDialog,
  useErrorDialog,
} from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { useSharedLinkStore } from "~/stores/sharedLink";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";

const { t } = useI18n();
const commonStore = useCommonStore();
const sharedLinkStore = useSharedLinkStore();
const { isLoading, withLoading } = useLoadingQueue();

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

const fetchData = async () => {
  await withLoading(async () => {
    try {
      await sharedLinkStore.fetchSharedLinks();
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
};

onMounted(async () => await fetchData());

const dataTypeOptions = [
  { value: SharedLinkDataTypesEnum.Salary, label: "Salary" },
  { value: SharedLinkDataTypesEnum.Qualification, label: "Qualification" },
  { value: SharedLinkDataTypesEnum.Vocabulary, label: "Vocabulary" },
];

const showCreateModal = ref(false);
const newDataTypes = ref<SharedLinkDataTypesEnum[]>([]);
const newExpiresAt = ref("");

const toggleDataType = (value: SharedLinkDataTypesEnum) => {
  if (newDataTypes.value.includes(value)) {
    newDataTypes.value = newDataTypes.value.filter(
      (v: SharedLinkDataTypesEnum) => v !== value,
    );
  } else {
    newDataTypes.value.push(value);
  }
};

const onCloseCreateModal = () => {
  showCreateModal.value = false;
  newDataTypes.value = [];
  newExpiresAt.value = "";
};

const onCreateLink = async () => {
  await withLoading(async () => {
    try {
      await sharedLinkStore.postSharedLink({
        dataTypes:
          newDataTypes.value as unknown as Set<SharedLinkDataTypesEnum>,
        expiresAt: newExpiresAt.value,
      });
      onCloseCreateModal();
      await fetchData();
      await openInfoDialog(t("message.info.completeSuccessfully"));
    } catch (err) {
      console.error(err);
      await openErrorDialog(t("message.error.unexpectedError"));
    }
  });
};

const onDelete = async (token: string) => {
  const confirmed = await openConfirmDialog(
    "Confirm deletion of this shared link?",
  );
  if (!confirmed) return;
  await withLoading(async () => {
    try {
      await sharedLinkStore.deleteSharedLink(token);
      await fetchData();
    } catch (err) {
      console.error(err);
      await openErrorDialog(t("message.error.somethingCloudNotDeleted"));
    }
  });
};

type SharedLinkRow = SharedLink & {
  index: number;
  dataTypesBadges: string[];
  statusLabel: string;
} & Record<string, unknown>;

const rows = computed<SharedLinkRow[]>(() =>
  (sharedLinkStore.sharedLinks ?? []).map((link: SharedLink, i: number) => ({
    ...link,
    index: i + 1,
    dataTypesBadges: Array.from(link.dataTypes),
    statusLabel:
      link.expiresAt && link.expiresAt < new Date().toISOString().slice(0, 10)
        ? "expired"
        : "active",
  })),
);

const columnDefs: ColumnDef<SharedLinkRow>[] = [
  {
    field: "index",
    header: "",
    sortable: true,
    headerClass: "w-8",
    bodyClass: "text-center h-12",
  },
  {
    field: "shareUrl",
    header: "Share URL",
    sortable: false,
    headerClass: "w-80",
    bodyClass: "h-12 truncate",
  },
  {
    field: "dataTypesBadges",
    header: "Data Types",
    sortable: false,
    headerClass: "w-56",
    bodyClass: "h-12",
  },
  {
    field: "expiresAt",
    header: "Expires At",
    sortable: true,
    headerClass: "w-30",
    bodyClass: "text-center h-12",
  },
  {
    field: "statusLabel",
    header: "Status",
    sortable: true,
    headerClass: "w-24",
    bodyClass: "text-center h-12 font-bold",
    bodyClassFunction: (value: unknown) =>
      value === "expired" ? "text-red-600" : "text-green-600",
  },
  {
    header: "Action",
    actionButtons: ["delete"],
    onClickDelete: (row: SharedLinkRow) => {
      if (row.token) onDelete(row.token);
    },
    headerClass: "w-20",
    bodyClass: "h-12",
    iconClass: "w-5 h-5 text-red-600 translate-y-1",
  },
];

const initSortState: SortDef<SharedLinkRow> = {
  column: "index",
  direction: "asc",
};
</script>
