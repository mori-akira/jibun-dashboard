<template>
  <div
    :class="[wrapperClass]"
    tabindex="0"
    @click="onClick"
    @keydown.enter="onClick"
  >
    <Icon
      v-if="type === 'plus'"
      name="tabler:plus"
      :class="['icon', iconClass]"
    />
    <Icon
      v-if="type === 'cancel'"
      name="tabler:x"
      :class="['icon', iconClass]"
    />
    <Icon
      v-if="type === 'edit'"
      name="tabler:database-edit"
      :class="['icon', iconClass]"
    />
    <Icon
      v-if="type === 'delete'"
      name="tabler:trash"
      :class="['icon', iconClass]"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  type: "plus" | "cancel" | "edit" | "delete";
  wrapperClass?: string;
  iconClass?: string;
}>();

const emit = defineEmits<{
  (event: "click"): void;
}>();

const onClick = (): void => {
  emit("click");
};
</script>

<style lang="css" scoped>
.icon:hover {
  cursor: pointer;
  color: brightness(0.7);
}
</style>
