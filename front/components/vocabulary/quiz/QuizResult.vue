<template>
  <div>
    <Panel>
      <h3>
        <Icon name="tabler:trophy" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Result</span>
      </h3>
      <div class="flex flex-col items-center gap-4 mt-6 mb-4">
        <div class="flex gap-12 text-center">
          <div>
            <p class="text-4xl font-bold text-gray-800">{{ correctCount }}</p>
            <p class="text-sm font-cursive text-gray-500 mt-1">Correct</p>
          </div>
          <div class="text-4xl font-bold text-gray-300">/</div>
          <div>
            <p class="text-4xl font-bold text-gray-800">{{ totalCount }}</p>
            <p class="text-sm font-cursive text-gray-500 mt-1">Total</p>
          </div>
        </div>
        <div class="w-64 bg-gray-200 rounded-full h-3 mt-2">
          <div
            class="bg-gray-800 h-3 rounded-full"
            :style="{ width: `${(correctCount / totalCount) * 100}%` }"
          />
        </div>
      </div>
    </Panel>

    <div class="flex justify-end gap-3 mr-4 mt-4">
      <Button
        type="action"
        size="normal"
        button-class="w-40"
        :disabled="submitted || isLoading"
        @click:button="$emit('submit')"
      >
        <Icon name="tabler:send" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">{{
          submitted ? "Submitted" : "Submit"
        }}</span>
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";

defineProps<{
  correctCount: number;
  totalCount: number;
  submitted: boolean;
  isLoading: boolean;
}>();

defineEmits<{
  (e: "submit"): void;
}>();
</script>
