<template>
  <a
    v-if="target === '_blank'"
    :href="link"
    :class="[anchorClass]"
    :target="target"
  >
    <span>{{ text }}</span>
    <Icon
      v-if="!noIcon"
      :name="iconName ?? 'tabler:external-link'"
      :class="iconClass ?? 'link-icon'"
    />
  </a>
  <NuxtLink
    v-else-if="target === '_self'"
    class="text-gray-900"
    :to="link"
    :class="[anchorClass]"
    :target="target"
  >
    <Icon
      v-if="!noIcon"
      :name="iconName ?? 'tabler:math-greater'"
      :class="iconClass ?? 'link-icon'"
    />
    <span class="font-cursive ml-2">{{ text }}</span>
  </NuxtLink>
</template>

<script setup lang="ts">
defineProps<{
  link: string;
  text: string;
  target?: "_blank" | "_self" | "_parent" | "_top";
  iconName?: string;
  noIcon?: boolean;
  anchorClass?: string;
  iconClass?: string;
}>();
</script>

<style lang="css" scoped>
a {
  color: #33f;
  text-decoration: underline;
}

.link-icon {
  transform: translate(2px, 3px);
}
</style>
