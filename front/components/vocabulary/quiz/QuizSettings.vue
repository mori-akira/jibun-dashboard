<template>
  <Panel>
    <h3>
      <Icon name="tabler:settings" class="adjust-icon-4" />
      <span class="font-cursive font-bold ml-2">Quiz Settings</span>
    </h3>
    <div class="flex flex-col gap-4 mt-4 ml-4">
      <!-- Tags -->
      <div
        :class="
          layout === 'vertical'
            ? 'flex flex-col gap-2'
            : 'flex items-center gap-4'
        "
      >
        <span class="font-cursive w-32 shrink-0">Tags</span>
        <MultiOptionSelector
          v-if="vocabularyTags.length > 0"
          label=""
          :options="vocabularyTags.map((t) => t.vocabularyTag)"
          :values="vocabularyTags.map((t) => t.vocabularyTagId ?? '')"
          :selected-options="selectedTagIds"
          wrapper-class="flex flex-wrap gap-1"
          @click:value="$emit('click:tag', $event)"
        />
        <span v-else class="text-gray-500 text-sm"
          >No tags available (all vocabularies in scope)</span
        >
      </div>

      <!-- Direction -->
      <div
        :class="
          layout === 'vertical'
            ? 'flex flex-col gap-2'
            : 'flex items-center gap-4'
        "
      >
        <span class="font-cursive w-32 shrink-0">Direction</span>
        <div
          :class="layout === 'vertical' ? 'flex flex-col gap-2' : 'flex gap-6'"
        >
          <label
            v-for="d in (['FRONT_TO_BACK', 'BACK_TO_FRONT'] as const)"
            :key="d"
            class="flex items-center gap-2 cursor-pointer"
          >
            <input
              :checked="direction === d"
              type="radio"
              :value="d"
              class="accent-gray-800"
              @change="$emit('update:direction', d)"
            />
            <span>{{ getDirectionLabel(d) }}</span>
          </label>
        </div>
      </div>

      <!-- Count -->
      <div
        :class="
          layout === 'vertical'
            ? 'flex flex-col gap-2'
            : 'flex items-center gap-4'
        "
      >
        <span class="font-cursive w-32 shrink-0">Count</span>
        <div class="flex items-center gap-2">
          <SelectBox
            :model-value="String(questionCount)"
            :options="
              countOptions.map((n) => ({ label: String(n), value: String(n) }))
            "
            :placeholder="
              countOptions.length === 0 ? 'No vocabularies in scope' : undefined
            "
            select-wrapper-class="w-24"
            @update:model-value="(v: string) => $emit('update:questionCount', Number(v))"
          />
          <span class="text-sm text-gray-500 ml-4"
            >/ {{ scopeCount }} in scope</span
          >
        </div>
      </div>
    </div>
    <div class="flex justify-center mr-4 mt-4">
      <Button
        type="action"
        size="normal"
        button-class="w-40"
        :disabled="countOptions.length === 0 || isLoading"
        @click:button="$emit('start')"
      >
        <Icon name="tabler:player-play" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Start Quiz</span>
      </Button>
    </div>
  </Panel>
</template>

<script setup lang="ts">
import type { VocabularyTag } from "~/generated/api/client/api";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import MultiOptionSelector from "~/components/common/MultiOptionSelector.vue";
import { type QuizDirection, getDirectionLabel } from "~/utils/vocabularyQuiz";

defineProps<{
  vocabularyTags: VocabularyTag[];
  selectedTagIds: string[];
  direction: QuizDirection;
  questionCount: number;
  countOptions: number[];
  scopeCount: number;
  isLoading: boolean;
  layout?: "horizontal" | "vertical";
}>();

defineEmits<{
  (e: "click:tag", tagId: string): void;
  (e: "update:direction", value: QuizDirection): void;
  (e: "update:questionCount", value: number): void;
  (e: "start"): void;
}>();
</script>
