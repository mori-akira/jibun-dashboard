<template>
  <div :class="['wrapper', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div
      v-for="(option, index) in options"
      :key="index"
      :class="[
        'option',
        optionClass,
        { selected: selectedOptions.includes(values[index]) },
      ]"
      :value="values[index]"
      role="button"
      tabindex="0"
      @click="onClickOption(values[index])"
      @keydown.enter="onClickOption(values[index])"
    >
      {{ option }}
    </div>
  </div>
</template>

<script setup lang="ts">
import Label from "~/components/common/Label.vue";

defineProps<{
  label?: string;
  required?: boolean;
  options: string[];
  values: string[];
  selectedOptions: string[];
  wrapperClass?: string;
  labelClass?: string;
  optionClass?: string;
}>();

const emit = defineEmits<{
  (event: "click:value", value: string): void;
}>();

const onClickOption = (value: string) => {
  emit("click:value", value);
};
</script>

<style scoped>
.option {
  margin-left: 1rem;
  padding: 0.2rem 1rem;
  font-size: 0.8rem;
  background-color: #888;
  color: #fff;
  border-radius: 0.5rem;
}

.option.selected {
  background-color: #bb88ff;
}

.option:hover {
  cursor: pointer;
  color: #eee;
}

.option:not(.selected):hover {
  background-color: #777;
}

.option.selected:hover {
  background-color: #aa77ee;
}
</style>
