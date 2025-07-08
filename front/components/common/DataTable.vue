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
          >
            <div class="header-inner">
              <div>{{ column.header }}</div>
              <div v-show="column.sortable" class="sort-button-group">
                <Icon
                  name="tabler:triangle-filled"
                  :class="['sort-button', 'asc', {selected: sortState?.column === column.field && sortState?.direction === 'asc'}]"
                  @click="onChangeSortState({ column: column.field, direction: 'asc'})"
                />
                  <Icon
                  name="tabler:triangle-inverted-filled"
                  :class="['sort-button', 'desc', {selected: sortState?.column === column.field && sortState?.direction === 'desc'}]"
                  @click="onChangeSortState({ column: column.field, direction: 'desc'})"
                />
              </div>
            </div>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, index) in displayRows" :key="index" :class="[bodyClass, {clickable: rowClickable}]">
          <td
            v-for="(def, index2) in columnDefs"
            :key="`body-${index}-${index2}`"
            :class="[
              bodyClass,
              def.bodyClass,
              def.bodyClassFunction?.((row as Record<string, any>)[def.field], row as Record<string, any>)
            ]"
            @click="onClickRow((row as Record<string, any>)[rowActionKey as string])"
          >
            {{ (row as Record<string, any>)[def.field] }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
// 型定義
export type ColumnDef = {
  field: string;
  header: string;
  sortable?: boolean;
  headerClass?: string;
  bodyClass?: string;
  bodyClassFunction?: (value: unknown, row: Record<string, unknown>) => string;
};
export type SortDef = {
  column: string;
  direction: "asc" | "desc";
}

// プロパティ定義
const props = defineProps<{
  rows: unknown[];
  columnDefs: ColumnDef[];
  isLoading?: boolean;
  rowClickable?: boolean;
  rowActionKey?: string;
  wrapperClass?: string;
  initSortState?: SortDef;
  tableClass?: string;
  headerClass?: string;
  bodyClass?: string;
}>();
const emit = defineEmits<{
  (event: "click:row", value:unknown): void;
}>();

// 行アクション
const onClickRow = (value: unknown) => {
  if (!props.rowClickable || !props.rowActionKey) {
    return;
  }
  emit("click:row", value);
}

// ソート
const sortState = ref<SortDef | null>(props.initSortState || null);
const onChangeSortState = (value: SortDef) => {
  sortState.value = value;
}
const sortRows = (rows: unknown[]): unknown[] => {
  if (!sortState.value) {
    return [...rows];
  }

  const { column, direction } = sortState.value;
  return [...rows].sort((a, b) => {
    const aVal = (a as Record<string, unknown>)[column];
    const bVal = (b as Record<string, unknown>)[column];

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
const displayRows = ref<unknown[]>(sortRows(props.rows || []));
watch(
  () => sortState.value,
  () => {
    displayRows.value = sortRows(props.rows);
  },
  { immediate: true }
);
</script>

<style scoped>
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
