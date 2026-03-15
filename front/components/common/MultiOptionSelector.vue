<template>
  <div :class="['wrapper', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div
      v-for="(option, index) in options"
      :key="index"
      :class="[
        'py-[0.2rem] px-4 text-[0.8rem] bg-[#888] text-white rounded-lg hover:cursor-pointer hover:text-[#eee] [&.selected]:bg-[#bb88ff] [&:not(.selected)]:hover:bg-[#777] [&.selected]:hover:bg-[#aa77ee]',
        optionClass,
        { selected: selectedOptions.includes(values[index] ?? '') },
      ]"
      :value="values[index]"
      role="button"
      tabindex="0"
      @click="onClickOption(values[index] ?? '')"
      @keydown.enter="onClickOption(values[index] ?? '')"
    >
      {{ option }}
    </div>
  </div>
</template>

<script setup lang="ts">
import Label from "~/components/common/Label.vue";

withDefaults(
  defineProps<{
    label?: string;
    required?: boolean;
    options: string[];
    values: string[];
    selectedOptions: string[];
    wrapperClass?: string;
    labelClass?: string;
    optionClass?: string;
  }>(),
  { label: "", optionClass: "ml-4", wrapperClass: "", labelClass: "" },
);

const emit = defineEmits<{
  (event: "click:value", value: string): void;
}>();

const onClickOption = (value: string) => {
  emit("click:value", value);
};
</script>
