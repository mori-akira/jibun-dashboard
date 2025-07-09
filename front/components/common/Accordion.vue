<template>
  <div :class="['wrapper', wrapperClass]">
    <div :class="['header', headerClass]" @click="onClickHeader">
      <Icon
        name="tabler:triangle-inverted-filled"
        :class="['toggle-icon', { opened: isOpened }]"
      />
      <span :class="['title', titleClass]">{{ title }}</span>
    </div>
    <div class="body-wrapper">
      <div :class="['body', bodyClass, { opened: isOpened }]">
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
  bodyClass?: string;
}>();

const isOpened = ref<boolean>(props.initOpened || false);
const onClickHeader = () => (isOpened.value = !isOpened.value);
</script>

<style scoped>
.header {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  border-radius: 0.5rem;
}

.header:hover {
  cursor: pointer;
  background-color: #eee;
}

.toggle-icon {
  font-size: 0.7rem;
  color: #333;
  transform: rotate(-90deg);
  transition: 0.5s;
}

.toggle-icon.opened {
  transform: rotate(0);
}

.title {
  margin-left: 0.5rem;
}

.body-wrapper {
  overflow: hidden;
}

.body {
  transition: 0.5s;
  height: 0;
  opacity: 0;
}

.body.opened {
  height: auto;
  opacity: 1;
}
</style>
