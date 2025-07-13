<template>
  <div :class="['wrapper', wrapperClass]">
    <div
      v-for="(def, index) in itemDefs"
      v-show="def.skipIfNull && typedItem[def.field]"
      :key="index"
      :class="['row', rowClass]"
    >
      <div :class="['label', labelClass, def.labelClass]">{{ def.label }}</div>
      <div
        :class="[
          'item',
          itemClass,
          def.itemClass,
          def.itemClassFunction?.(typedItem[def.field], typedItem),
        ]"
        :style="{
          ...def.itemStyle,
          ...def.itemStyleFunction?.(typedItem[def.field], typedItem),
        }"
      >
        <span v-if="def.itemType === 'plainText'">{{
          typedItem[def.field]
        }}</span>
        <AnchorLink
          v-else-if="def.itemType === 'anchorLink'"
          :link="(typedItem)[def.field] as string"
          :text="(typedItem)[def.field] as string"
          target="_blank"
          anchor-class="truncate"
        />
        <span v-else-if="def.itemType === undefined">{{
          typedItem[def.field]
        }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AnchorLink from "~/components/common/AnchorLink.vue";

export type ItemDef = {
  field: string;
  label: string;
  skipIfNull?: boolean;
  itemType?: "plainText" | "anchorLink";
  labelClass?: string;
  itemClass?: string;
  itemClassFunction?: (value: unknown, row: Record<string, unknown>) => string;
  itemStyle?: Record<string, string>;
  itemStyleFunction?: (
    value: unknown,
    row: Record<string, unknown>
  ) => Record<string, string>;
};

const props = defineProps<{
  item: unknown;
  itemDefs: ItemDef[];
  wrapperClass?: string;
  rowClass?: string;
  labelClass?: string;
  itemClass?: string;
}>();

const typedItem = computed(() => props.item as Record<string, unknown>);
</script>

<style lang="css" scoped>
.row {
  display: flex;
  justify-content: center;
  width: 100%;
  height: 2rem;
  margin-top: 0.1rem;
}

.label {
  display: flex;
  align-items: center;
  padding: 0.5rem;
}

.item {
  display: flex;
  align-items: center;
  margin-left: 0.1rem;
  padding: 0.5rem;
  text-wrap: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
