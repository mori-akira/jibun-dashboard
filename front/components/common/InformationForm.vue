<template>
  <div :class="[wrapperClass]">
    <div
      v-for="(def, index) in itemDefs"
      v-show="def.skipIfNull && typedItem[def.field]"
      :key="index"
      :class="['flex justify-center w-full h-8 mt-[0.1rem]', rowClass]"
    >
      <div :class="['flex items-center p-2', labelClass, def.labelClass]">
        {{ def.label }}
      </div>
      <div
        :class="[
          'flex items-center ml-[0.1rem] p-2 whitespace-nowrap overflow-hidden text-ellipsis',
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
        <AnchorLink
          v-else-if="def.itemType === 'asyncLink'"
          :text="(def.asyncLinkText ?? typedItem[def.field]) as string"
          :async-link="() => def.asyncLinkFunction!(typedItem[def.field], typedItem)"
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
  itemType?: "plainText" | "anchorLink" | "asyncLink";
  asyncLinkText?: string;
  asyncLinkFunction?: (
    value: unknown,
    row: Record<string, unknown>,
  ) => Promise<string>;
  labelClass?: string;
  itemClass?: string;
  itemClassFunction?: (value: unknown, row: Record<string, unknown>) => string;
  itemStyle?: Record<string, string>;
  itemStyleFunction?: (
    value: unknown,
    row: Record<string, unknown>,
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
