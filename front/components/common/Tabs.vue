<template>
  <div :class="wrapperClass">
    <TabList
      :tabs="tabs"
      :active-index="activeIndex"
      :wrapper-class="tabListWrapperClass"
      :button-class="buttonClass"
      @change="setActiveIndex"
    />
    <div
      v-for="(tab, index) in tabs"
      v-show="index === activeIndex"
      :key="index"
    >
      <slot :name="tab.slot" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import TabList from "~/components/common/TabList.vue";

const props = defineProps<{
  tabs: { label: string; slot: string }[];
  initTab?: string;
  wrapperClass?: string;
  tabListWrapperClass?: string;
  buttonClass?: string;
}>();

const emits = defineEmits<{
  (e: "change:tab", slot: string): void;
}>();

const initialIndex = computed(() =>
  props.initTab ? props.tabs.findIndex((tab) => tab.slot === props.initTab) : 0
);
const activeIndex = ref(initialIndex.value);
const setActiveIndex = (index: number) => {
  activeIndex.value = index;
  emits("change:tab", props.tabs[index]?.slot ?? "");
};
</script>
