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

defineProps<{
  tabs: { label: string; slot: string }[];
  wrapperClass?: string;
  tabListWrapperClass?: string;
  buttonClass?: string;
}>();

const activeIndex = ref(0);
const setActiveIndex = (index: number) => {
  activeIndex.value = index;
};
</script>
