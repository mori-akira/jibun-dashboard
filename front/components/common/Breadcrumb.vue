<template>
  <nav class="flex items-center">
    <ul class="flex items-center">
      <li v-for="(item, index) in items" :key="index" class="flex items-center">
        <Icon
          v-if="index > 0"
          name="tabler:math-greater"
          class="adjust-icon-2 mx-4"
        />
        <Icon
          v-if="item.iconName"
          :name="item.iconName"
          class="adjust-icon-2"
        />
        <template v-if="item.link">
          <AnchorLink
            :link="item.link"
            :text="item.text"
            :no-icon="true"
            anchor-class="font-cursive font-bold"
            target="_self"
          />
        </template>
        <template v-else>
          <span class="font-cursive font-bold ml-2">
            {{ item.text }}
          </span>
        </template>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import AnchorLink from "~/components/common/AnchorLink.vue"; // パスは調整してください
import type { PropType } from "vue";

type BreadcrumbItem = {
  text: string;
  iconName?: string;
  link?: string;
};

defineProps({
  items: {
    type: Array as PropType<BreadcrumbItem[]>,
    required: true,
  },
});
</script>
