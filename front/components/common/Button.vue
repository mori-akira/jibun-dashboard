<template>
  <div :class="['wrapper', wrapperClass]">
    <button
      :class="[
        buttonClass,
        size,
        { 'action-button': type === 'action' || type === 'marked' },
        { 'add-button': type === 'add' },
        { 'delete-button': type === 'delete' },
        { 'navigation-button': type === 'navigation' },
      ]"
      :disabled="disabled"
      :type="htmlType || 'button'"
      @click="onClick"
    >
      <slot />
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  type?: "action" | "marked" | "navigation" | "add" | "delete" | "default";
  size?: "small" | "normal";
  htmlType?: "button" | "submit" | "reset";
  disabled?: boolean;
  wrapperClass?: string;
  buttonClass?: string;
}>();

const emit = defineEmits<{
  (event: "click"): void;
}>();

const onClick = (): void => {
  emit("click");
};
</script>

<style lang="css" scoped>
button {
  border: 3px solid #ddd;
  background-color: #666;
  color: #fff;
  padding: 0.2rem 1rem;
  border-radius: 0.5rem;
  box-shadow: 1px 1px 2px #000;
}

button.small {
  font-size: 0.9rem;
  padding: 0.1rem 0.5rem;
}

button[disabled] {
  border: 3px solid #bbb;
  color: #bbb;
  box-shadow: none;
  transform: translate(1px, 1px);
}

button:not([disabled]):hover {
  cursor: pointer;
  box-shadow: none;
  transform: translate(1px, 1px);
}

button.action-button {
  background-color: #88f;
}

button.add-button {
  background-color: #f88;
}

button.delete-button {
  background-color: #888;
}

button.navigation-button {
  background-color: #888;
}
</style>
