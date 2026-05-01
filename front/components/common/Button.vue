<template>
  <div :class="[wrapperClass]">
    <button
      :class="[
        'py-[0.2rem] px-4 rounded-lg shadow-[1px_1px_2px_#000] border-[3px] border-[#ddd] text-white',
        { 'text-[0.9rem] py-[0.1rem] px-2': size === 'small' },
        {
          '!border-[#bbb] !text-[#bbb] !shadow-none !translate-x-px !translate-y-px':
            disabled,
        },
        {
          'hover:cursor-pointer hover:shadow-none hover:translate-x-px hover:translate-y-px':
            !disabled,
        },
        { 'bg-[#88f]': type === 'action' || type === 'marked' },
        { 'bg-[#f88]': type === 'add' || type === 'error' },
        { 'bg-[#888]': type === 'delete' || type === 'navigation' },
        { 'bg-[#666]': type === 'default' || !type },
        buttonClass,
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
  type?:
    | "action"
    | "marked"
    | "navigation"
    | "add"
    | "delete"
    | "error"
    | "default";
  size?: "small" | "normal";
  htmlType?: "button" | "submit" | "reset";
  disabled?: boolean;
  wrapperClass?: string;
  buttonClass?: string;
}>();

const emit = defineEmits<{
  (event: "click:button"): void;
}>();

const onClick = (): void => {
  emit("click:button");
};
</script>
