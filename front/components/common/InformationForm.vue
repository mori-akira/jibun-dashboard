<template>
  <div :class="['wrapper', wrapperClass]">
    <div
      v-for="(def, index) in itemDefs"
      v-show="def.skipIfNull && (item as Record<string, unknown>)[def.field]"
      :key="index"
      :class="['row', rowClass]"
    >
      <div :class="['label', labelClass, def.labelClass]">{{ def.label }}</div>
      <div
        :class="[
          'item',
          itemClass,
          def.itemClass, def.itemClassFunction?.((item as Record<string, unknown>)[def.field], item as Record<string, unknown>)
        ]"
      >
        <span v-show="def.itemType === 'plainText'">{{ (item as Record<string, unknown>)[def.field] }}</span>
        <AnchorLink
          v-show="def.itemType === 'anchorLink'"
          :link="(item as Record<string, unknown>)[def.field] as string"
          :text="(item as Record<string, unknown>)[def.field] as string"
          anchor-class="truncate"
        />
        <span v-show="def.itemType === undefined">{{ (item as Record<string, unknown>)[def.field] }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AnchorLink from '~/components/common/AnchorLink.vue';

export type ItemDef = {
  field: string;
  label: string;
  skipIfNull?: boolean;
  itemType?: "plainText" | "anchorLink";
  labelClass?: string;
  itemClass?: string;
  itemClassFunction?: (value: unknown, row: Record<string, unknown>) => string;
};

defineProps<{
  item: unknown;
  itemDefs: ItemDef[];
  wrapperClass?: string;
  rowClass?: string;
  labelClass?: string;
  itemClass?: string;
}>();
</script>

<style scoped>
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
