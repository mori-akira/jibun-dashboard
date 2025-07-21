<template>
  <div :class="['wrapper', wrapperClass]">
    <div v-show="isLoading" class="loading-overlay">
      <Icon name="tabler:loader" class="loading-spinner" />
    </div>
    <table :class="[tableClass]">
      <thead>
        <tr>
          <th
            v-for="(column, index) in columnDefs"
            :key="`header-${index}`"
            :class="[headerClass, column.headerClass]"
            :aria-selected="sortState?.column === column.field"
            :aria-sort="
              sortState?.column === column.field
                ? sortState?.direction === 'desc'
                  ? 'descending'
                  : 'ascending'
                : undefined
            "
          >
            <div class="header-inner">
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
              <div v-show="column.sortable" class="sort-button-group">
                <Icon
                  name="tabler:triangle-filled"
                  :class="[
                    'sort-button',
                    'asc',
                    {
                      selected:
                        sortState?.column === column.field &&
                        sortState?.direction === 'asc',
                    },
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
                    'sort-button',
                    'desc',
                    {
                      selected:
                        sortState?.column === column.field &&
                        sortState?.direction === 'desc',
                    },
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
          :class="[bodyClass, { clickable: rowClickable }]"
          @click="rowActionKey && onClickRow(row)"
        >
          <td
            v-for="(def, index2) in columnDefs"
            :key="`body-${index}-${index2}`"
            :class="[
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
            <span>{{ def.field ? row[def.field] : "" }}</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, unknown>">
import CheckBox from "~/components/common/CheckBox.vue";
import type { CheckBoxStatus } from "~/components/common/CheckBox.vue";

// 型定義
export type ColumnDef<T> = {
  field?: keyof T;
  header?: string;
  sortable?: boolean;
  checkable?: boolean;
  checkStatusFunction?: (row: T) => CheckBoxStatus;
  headerCheckable?: boolean;
  headerCheckStatusFunction?: (rows: T[]) => CheckBoxStatus;
  headerClass?: string;
  bodyClass?: string;
  bodyClassFunction?: (value: unknown, row: T) => string;
  bodyStyle?: Record<string, string>;
  bodyStyleFunction?: (value: unknown, row: T) => Record<string, string>;
  onChangeCheck?: (status: CheckBoxStatus, row: T) => void;
  onChangeHeaderCheck?: (status: CheckBoxStatus, rows: T[]) => void;
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
  rowActionKey?: keyof T;
  wrapperClass?: string;
  initSortState?: SortDef<T>;
  tableClass?: string;
  headerClass?: string;
  bodyClass?: string;
}>();
const emit = defineEmits<{
  (event: "click:row", row: T): void;
}>();

// 行アクション
const onClickRow = (row: T) => {
  if (!props.rowClickable || !props.rowActionKey) {
    return;
  }
  emit("click:row", row);
};

// ソート
const sortState = ref<SortDef<T> | null>(props.initSortState || null);
const onChangeSortState = (value: SortDef<T>) => {
  sortState.value = value;
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

// 表示行
const displayRows = ref<T[]>(sortRows(props.rows ?? []));
watch(
  () => [props.rows, sortState.value],
  () => {
    displayRows.value = sortRows(props.rows);
  },
  { immediate: true }
);
</script>

<style lang="css" scoped>
.wrapper {
  position: relative;
}

table {
  width: 100%;
  table-layout: fixed;
}

th {
  padding: 0.2rem 1rem;
  border-right: 1px solid #fff;
}

th > .header-inner {
  display: flex;
  justify-content: center;
  align-items: center;
}

th > .header-inner > .sort-button-group {
  width: 0.6rem;
  margin: 0 0.5rem;
}

th > .header-inner > .sort-button-group > .sort-button {
  font-size: 0.6rem;
}

th > .header-inner > .sort-button-group > .sort-button:hover {
  cursor: pointer;
  color: #bbb;
}

th > .header-inner > .sort-button-group > .sort-button.selected {
  cursor: unset;
  color: #bbb;
}

th > .header-inner > .sort-button-group > .sort-button.asc {
  transform: translateY(6px);
}

th > .header-inner > .sort-button-group > .sort-button.desc {
  transform: translateY(-6px);
}

tbody > tr.clickable:hover {
  cursor: pointer;
  background-color: #efe;
}

td {
  padding: 0.2rem 0.5rem;
  border-bottom: 1px solid #333;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #ffffffdd;
  display: flex;
  justify-content: center;
}

.loading-spinner {
  width: 2rem;
  height: 2rem;
  margin-top: 2rem;
  animation: rotation 3s linear infinite;
}

@keyframes rotation {
  0% {
    transform: rotate(0);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
