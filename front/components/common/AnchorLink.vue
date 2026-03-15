<template>
  <span
    v-if="asyncLink"
    :class="[
      'cursor-pointer text-blue-600 underline flex items-center',
      anchorClass,
    ]"
    @click="onClickAsyncLink"
  >
    <span>{{ text }}</span>
    <Icon
      v-if="!noIcon"
      :name="iconName ?? 'tabler:external-link'"
      :class="iconClass ?? 'translate-x-0.5 translate-y-0.5'"
    />
  </span>
  <a
    v-else-if="target === '_blank'"
    :href="link"
    :class="['text-blue-600 underline', anchorClass]"
    :target="target"
  >
    <span>{{ text }}</span>
    <Icon
      v-if="!noIcon"
      :name="iconName ?? 'tabler:external-link'"
      :class="iconClass ?? 'translate-x-0.5 translate-y-0.5'"
    />
  </a>
  <NuxtLink
    v-else-if="target === '_self'"
    class="text-gray-900"
    :to="link ?? ''"
    :class="[anchorClass]"
    :target="target"
  >
    <Icon
      v-if="!noIcon"
      :name="iconName ?? 'tabler:math-greater'"
      :class="iconClass ?? 'translate-x-0.5 translate-y-0.5'"
    />
    <span class="font-cursive text-blue-600 underline ml-2">{{ text }}</span>
  </NuxtLink>
</template>

<script setup lang="ts">
const props = defineProps<{
  link?: string;
  text: string;
  target?: "_blank" | "_self" | "_parent" | "_top";
  asyncLink?: () => Promise<string>;
  iconName?: string;
  noIcon?: boolean;
  anchorClass?: string;
  iconClass?: string;
}>();

const onClickAsyncLink = async () => {
  if (!props.asyncLink) return;
  const url = await props.asyncLink();
  window.open(url, "_blank");
};
</script>
