<template>
  <div class="grid grid-cols-3 items-center py-1 select-none text-sm">
    <div />
    <div class="flex items-center justify-center gap-1">
      <button
        :disabled="currentPage <= 1"
        class="hidden sm:block px-1.5 py-0.5 border border-gray-400 rounded disabled:opacity-30 hover:enabled:bg-gray-100 disabled:cursor-default"
        @click="emit('update:currentPage', 1)"
      >
        <Icon name="tabler:chevrons-left" class="adjust-icon-2" />
      </button>
      <button
        :disabled="currentPage <= 1"
        class="px-1.5 py-0.5 border border-gray-400 rounded disabled:opacity-30 hover:enabled:bg-gray-100 disabled:cursor-default"
        @click="emit('update:currentPage', currentPage - 1)"
      >
        <Icon name="tabler:chevron-left" class="adjust-icon-2" />
      </button>
      <span class="mx-2 min-w-12 text-center text-gray-600">
        {{ currentPage }} / {{ totalPages }}
      </span>
      <button
        :disabled="currentPage >= totalPages"
        class="px-1.5 py-0.5 border border-gray-400 rounded disabled:opacity-30 hover:enabled:bg-gray-100 disabled:cursor-default"
        @click="emit('update:currentPage', currentPage + 1)"
      >
        <Icon name="tabler:chevron-right" class="adjust-icon-2" />
      </button>
      <button
        :disabled="currentPage >= totalPages"
        class="hidden sm:block px-1.5 py-0.5 border border-gray-400 rounded disabled:opacity-30 hover:enabled:bg-gray-100 disabled:cursor-default"
        @click="emit('update:currentPage', totalPages)"
      >
        <Icon name="tabler:chevrons-right" class="adjust-icon-2" />
      </button>
    </div>
    <div class="hidden sm:flex justify-end">
      <select
        v-if="pageSizeOptions"
        :value="pageSize"
        class="border border-gray-400 rounded px-1 py-0.5 text-sm"
        @change="
          emit(
            'update:pageSize',
            Number(($event.target as HTMLSelectElement).value),
          )
        "
      >
        <option v-for="s in pageSizeOptions" :key="s" :value="s">
          {{ s }}
        </option>
      </select>
    </div>
  </div>
</template>

<script setup lang="ts">
export type PaginationProps = {
  currentPage: number;
  totalItems: number;
  pageSize: number;
  pageSizeOptions?: number[];
};

const props = defineProps<PaginationProps>();

const emit = defineEmits<{
  "update:currentPage": [page: number];
  "update:pageSize": [size: number];
}>();

const totalPages = computed(() =>
  Math.max(1, Math.ceil(props.totalItems / props.pageSize)),
);
</script>
