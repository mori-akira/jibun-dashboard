<template>
  <div :class="['cursor-pointer', wrapperClass]" @click="onClick">
    <Icon
      v-if="props.status === 'off'"
      name="tabler:square-dashed"
      :class="['adjust-icon-4', iconClass]"
    />
    <Icon
      v-if="props.status === 'on'"
      name="tabler:square-check-filled"
      :class="['adjust-icon-4', 'text-blue-500', iconClass]"
    />
    <Icon
      v-if="props.status === 'neutral'"
      name="tabler:square-minus-filled"
      :class="['adjust-icon-4', 'text-blue-500', iconClass]"
    />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  status: CheckBoxStatus;
  type: "onOff" | "hasNeutral";
  disabled?: boolean;
  wrapperClass?: string;
  iconClass?: string;
}>();
export type CheckBoxStatus = "on" | "off" | "neutral";
const emits = defineEmits<{
  (event: "change:status", value: CheckBoxStatus): void;
}>();

const onClick = () => {
  emits("change:status", props.status);
};
</script>
