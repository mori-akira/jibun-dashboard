<template>
  <div :class="['relative flex flex-col', wrapperClass]">
    <div
      v-show="isLoading"
      class="absolute inset-0 bg-white/90 flex justify-center z-[900]"
    >
      <Icon
        name="tabler:loader"
        class="w-8 h-8 mt-8 animate-spin [animation-duration:3s]"
      />
    </div>
    <Pagination
      v-if="pagingEnabled"
      class="mb-2"
      :current-page="currentPage"
      :total-items="rows.length"
      :page-size="currentPageSize"
      :page-size-options="pageSizeOptions"
      @update:current-page="(page: number) => (currentPage = page)"
      @update:page-size="onChangePageSize"
    />
    <table :class="['w-full table-fixed', tableClass]">
      <thead>
        <tr>
          <th
            v-for="(column, index) in columnDefs"
            :key="`header-${index}`"
            :class="[
              'py-[0.2rem] px-4 border-r border-white',
              headerClass,
              column.headerClass,
            ]"
            :style="column.headerStyle"
            :aria-selected="sortState?.column === column.field"
            :aria-sort="
              sortState?.column === column.field
                ? sortState?.direction === 'desc'
                  ? 'descending'
                  : 'ascending'
                : undefined
            "
          >
            <div class="flex justify-center items-center">
              <div>
                <CheckBox
                  v-if="column.headerCheckable"
                  :status="
                    (
                      column.headerCheckStatusFunction && column.headerCheckStatusFunction(
                        (displayRows as T[])
                      )
                    ) ?? 'off'
                  "
                  type="hasNeutral"
                  @change:status="
                    ($event) =>
                      column.onChangeHeaderCheck && column.onChangeHeaderCheck(
                        $event, (displayRows as T[])
                      )
                  "
                />
                <span>{{ column?.header ?? "" }}</span>
              </div>
              <div v-show="column.sortable" class="w-[0.6rem] mx-2">
                <Icon
                  name="tabler:triangle-filled"
                  :class="[
                    'text-[0.6rem] translate-y-[6px]',
                    sortState?.column === column.field &&
                    sortState?.direction === 'asc'
                      ? 'cursor-default text-[#bbb]'
                      : 'hover:cursor-pointer hover:text-[#bbb]',
                  ]"
                  @click="
                    onChangeSortState({
                      column: column.field!,
                      direction: 'asc',
                    })
                  "
                />
                <Icon
                  name="tabler:triangle-inverted-filled"
                  :class="[
                    'text-[0.6rem] -translate-y-[6px]',
                    sortState?.column === column.field &&
                    sortState?.direction === 'desc'
                      ? 'cursor-default text-[#bbb]'
                      : 'hover:cursor-pointer hover:text-[#bbb]',
                  ]"
                  @click="
                    onChangeSortState({
                      column: column.field!,
                      direction: 'desc',
                    })
                  "
                />
              </div>
            </div>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(row, index) in (displayRows as T[])"
          :key="index"
          :class="[
            bodyClass,
            rowClickable ? 'hover:cursor-pointer hover:bg-[#efe]' : '',
          ]"
          @click="onClickRow(row)"
        >
          <td
            v-for="(def, index2) in columnDefs"
            :key="`body-${index}-${index2}`"
            :class="[
              'py-[0.2rem] px-2 border-b border-[#333]',
              bodyClass,
              def.bodyClass,
              def?.field && def.bodyClassFunction?.(row[def.field], row),
            ]"
            :style="{
              ...def.bodyStyle,
              ...(def?.field && def.bodyStyleFunction?.(row[def.field], row)),
            }"
          >
            <CheckBox
              v-if="def.checkable"
              :status="
                (def.checkStatusFunction && def.checkStatusFunction(row)) ??
                'off'
              "
              type="onOff"
              @change:status="
                ($event) => def.onChangeCheck && def.onChangeCheck($event, row)
              "
            />
            <template v-if="def.field">
              <slot
                :name="`cell-${String(def.field)}`"
                :row="row"
                :value="row[def.field]"
              >
                <span>{{ row[def.field] }}</span>
              </slot>
            </template>
            <div
              v-if="def.actionButtons"
              class="flex justify-around items-center"
            >
              <template
                v-for="(actionButton, index3) in def.actionButtons"
                :key="`button-${index}-${index2}-${index3}`"
              >
                <IconButton
                  v-if="actionButton === 'edit'"
                  type="edit"
                  :icon-class="def.iconClass"
                  @click="() => def.onClickEdit && def.onClickEdit(row)"
                />
                <IconButton
                  v-if="actionButton === 'delete'"
                  type="delete"
                  :icon-class="def.iconClass"
                  @click="() => def.onClickDelete && def.onClickDelete(row)"
                />
              </template>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-show="rows.length === 0" class="w-full text-center p-4 border-b">
      <span>No Item</span>
    </div>
    <Pagination
      v-if="pagingEnabled"
      class="mt-2"
      :current-page="currentPage"
      :total-items="rows.length"
      :page-size="currentPageSize"
      :page-size-options="pageSizeOptions"
      @update:current-page="(page: number) => (currentPage = page)"
      @update:page-size="onChangePageSize"
    />
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, unknown>">
import CheckBox from "~/components/common/CheckBox.vue";
import IconButton from "./IconButton.vue";
import Pagination from "./Pagination.vue";
import type { CheckBoxStatus } from "~/components/common/CheckBox.vue";

// 型定義
export type ColumnDef<T> = {
  field?: keyof T;
  header?: string;
  sortable?: boolean;
  checkable?: boolean;
  checkStatusFunction?: (row: T) => CheckBoxStatus;
  onChangeCheck?: (status: CheckBoxStatus, row: T) => void;
  headerCheckable?: boolean;
  headerCheckStatusFunction?: (rows: T[]) => CheckBoxStatus;
  onChangeHeaderCheck?: (status: CheckBoxStatus, rows: T[]) => void;
  actionButtons?: ("edit" | "delete")[];
  onClickEdit?: (row: T) => void;
  onClickDelete?: (row: T) => void;
  headerClass?: string;
  headerStyle?: Record<string, string>;
  bodyClass?: string;
  bodyClassFunction?: (value: unknown, row: T) => string;
  bodyStyle?: Record<string, string>;
  bodyStyleFunction?: (value: unknown, row: T) => Record<string, string>;
  iconClass?: string;
};
export type SortDef<T> = {
  column: keyof T;
  direction: "asc" | "desc";
};

// プロパティ定義
const props = defineProps<{
  rows: T[];
  columnDefs: ColumnDef<T>[];
  isLoading?: boolean;
  rowClickable?: boolean;
  wrapperClass?: string;
  initSortState?: SortDef<T>;
  tableClass?: string;
  headerClass?: string;
  bodyClass?: string;
  pageSize?: number | number[];
}>();
const emit = defineEmits<{
  (event: "click:row", row: T): void;
}>();

// 行アクション
const onClickRow = (row: T) => {
  if (!props.rowClickable) {
    return;
  }
  emit("click:row", row);
};

// ソート
const sortState = ref<SortDef<T> | null>(props.initSortState || null);
const onChangeSortState = (value: SortDef<T>) => {
  sortState.value = value;
  currentPage.value = 1;
};
const sortRows = (rows: T[]): T[] => {
  if (!sortState.value) {
    return [...rows];
  }

  const { column, direction } = sortState.value;
  return [...rows].sort((a, b) => {
    const aVal = a[column as keyof T];
    const bVal = b[column as keyof T];

    if (aVal == null) {
      return 1;
    }
    if (bVal == null) {
      return -1;
    }

    if (aVal < bVal) {
      return direction === "asc" ? -1 : 1;
    }
    if (aVal > bVal) {
      return direction === "asc" ? 1 : -1;
    }
    return 0;
  });
};

// ページング
const pagingEnabled = computed(() => props.pageSize !== undefined);
const pageSizeOptions = computed(() =>
  Array.isArray(props.pageSize) ? (props.pageSize as number[]) : undefined,
);
const currentPageSize = ref<number>(
  Array.isArray(props.pageSize)
    ? props.pageSize[0] ?? 10
    : props.pageSize ?? 10,
);
const currentPage = ref(1);

const onChangePageSize = (size: number) => {
  currentPageSize.value = size;
  currentPage.value = 1;
};

watch(
  () => props.pageSize,
  (newVal: number | number[] | undefined) => {
    currentPageSize.value = Array.isArray(newVal)
      ? newVal[0] ?? 10
      : newVal ?? 10;
    currentPage.value = 1;
  },
);

// 表示行
const sortedRows = computed(() => sortRows(props.rows ?? []));

const displayRows = computed(() => {
  if (!pagingEnabled.value) return sortedRows.value;
  const start = (currentPage.value - 1) * currentPageSize.value;
  return sortedRows.value.slice(start, start + currentPageSize.value);
});

watch(
  () => props.rows,
  () => {
    currentPage.value = 1;
  },
);
</script>
