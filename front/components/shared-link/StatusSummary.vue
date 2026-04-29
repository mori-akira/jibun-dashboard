<template>
  <div :class="['px-12 py-3 flex justify-center', wrapperClass]">
    <div v-if="sharedLinks.length === 0" class="text-gray-400 text-xs">
      No data
    </div>
    <div v-else class="w-2/3 flex flex-col gap-2">
      <div
        v-for="item in statusItems"
        :key="item.status"
        class="flex items-center gap-2"
      >
        <span
          :class="[
            'shrink-0 py-[0.15rem] px-2 text-[0.9rem] text-white rounded-lg',
            item.colorClass,
          ]"
        >
          {{ item.status }}
        </span>
        <span
          :class="[
            'ml-auto font-bold text-[#bb88ff]',
            { 'cursor-pointer underline': onNavigate },
          ]"
          @click="onNavigate?.()"
        >
          {{ item.count }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SharedLink } from "~/generated/api/client/api";

const props = defineProps<{
  sharedLinks: SharedLink[];
  wrapperClass?: string;
  onNavigate?: () => void;
}>();

const today = new Date().toISOString().slice(0, 10);

const statusItems = computed(() => {
  const active = props.sharedLinks.filter(
    (l) => !l.expiresAt || l.expiresAt >= today,
  ).length;
  const expired = props.sharedLinks.filter(
    (l) => l.expiresAt && l.expiresAt < today,
  ).length;
  return [
    { status: "active", count: active, colorClass: "bg-green-600" },
    { status: "expired", count: expired, colorClass: "bg-red-600" },
  ];
});
</script>
