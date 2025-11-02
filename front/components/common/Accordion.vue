<template>
  <div :class="[wrapperClass]">
    <div
      :class="[
        'inline-block py-[0.2rem] px-[0.5rem] rounded-lg hover:bg-[#eee] cursor-pointer',
        headerClass,
      ]"
      @click="onClickHeader"
    >
      <Icon
        name="tabler:triangle-inverted-filled"
        :class="[
          'text-[0.7rem] text-[#333] -rotate-90 transition duration-500',
          { 'rotate-0': isOpened },
        ]"
      />
      <span :class="['ml-2', titleClass]">{{ title }}</span>
    </div>
    <div
      :class="[
        'overflow-hidden transition-[max-height] duration-500',
        isOpened ? 'max-h-[1000px]' : 'max-h-0',
        bodyWrapperClass,
      ]"
    >
      <div
        :class="[
          'transition duration-500',
          isOpened ? 'opacity-100' : 'opacity-0',
          bodyClass,
        ]"
      >
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  title?: string;
  initOpened?: boolean;
  wrapperClass?: string;
  headerClass?: string;
  titleClass?: string;
  bodyWrapperClass?: string;
  bodyClass?: string;
}>();

const isOpened = ref<boolean>(props.initOpened || false);
const onClickHeader = () => (isOpened.value = !isOpened.value);
</script>
