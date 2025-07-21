<template>
  <div
    :class="['wrapper', 'cursor-pointer', wrapperClass]"
    @click="onClick(status)"
  >
    <Icon
      v-if="status === 'off'"
      name="tabler:square-dashed"
      :class="['adjust-icon-4', iconClass]"
    />
    <Icon
      v-if="status === 'on'"
      name="tabler:square-check-filled"
      :class="['adjust-icon-4', 'text-blue-500', iconClass]"
    />
    <Icon
      v-if="status === 'neutral'"
      name="tabler:square-minus-filled"
      :class="['adjust-icon-4', 'text-blue-500', iconClass]"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
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

const onClick = (status: CheckBoxStatus) => {
  emits("change:status", status);
};
</script>
