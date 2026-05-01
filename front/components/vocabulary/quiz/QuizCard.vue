<template>
  <Panel>
    <div class="flex justify-between items-center mb-4">
      <h3>
        <Icon name="tabler:cards" class="adjust-icon-4" />
        <span class="font-cursive font-bold ml-2">Quiz</span>
      </h3>
      <span class="font-cursive text-lg font-bold text-gray-600">
        {{ currentIndex + 1 }} / {{ totalCount }}
      </span>
    </div>

    <!-- Progress bar -->
    <div class="w-full bg-gray-200 rounded-full h-2 mb-6">
      <div
        class="bg-gray-800 h-2 rounded-full transition-all duration-300"
        :style="{ width: `${((currentIndex + 1) / totalCount) * 100}%` }"
      />
    </div>

    <!-- Flashcard -->
    <div class="flex justify-center mb-6">
      <Flashcard
        :key="currentIndex"
        :flipped="flipped"
        :card-class="flashcardClass"
        @click="$emit('flip')"
      >
        <template #front>
          <p
            :class="direction === 'FRONT_TO_BACK' ? 'text-center' : 'text-left'"
            class="text-xs text-gray-400 mb-2 font-cursive"
          >
            {{ direction === "FRONT_TO_BACK" ? "Name" : "Description" }}
          </p>
          <p
            :class="
              direction === 'FRONT_TO_BACK'
                ? 'text-lg font-bold text-center'
                : 'text-sm text-left'
            "
            class="whitespace-pre-wrap"
          >
            {{ frontText }}
          </p>
        </template>
        <template #back>
          <p
            :class="direction === 'FRONT_TO_BACK' ? 'text-left' : 'text-center'"
            class="text-xs text-gray-400 mb-2 font-cursive"
          >
            {{ direction === "FRONT_TO_BACK" ? "Description" : "Name" }}
          </p>
          <p
            :class="
              direction === 'FRONT_TO_BACK'
                ? 'text-sm text-left'
                : 'text-lg font-bold text-center'
            "
            class="whitespace-pre-wrap"
          >
            {{ backText }}
          </p>
        </template>
      </Flashcard>
    </div>

    <!-- Answer Buttons (after flip) -->
    <div
      v-if="flipped"
      :class="['flex justify-center gap-4 mb-4', answerButtonsClass]"
    >
      <Button
        :type="result === 'CORRECT' ? 'action' : 'default'"
        size="small"
        button-class="w-36"
        :disabled="result !== null"
        @click:button="$emit('answer', 'CORRECT')"
      >
        <Icon name="tabler:circle-check" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Correct</span>
      </Button>
      <Button
        :type="result === 'INCORRECT' ? 'error' : 'default'"
        size="small"
        button-class="w-36"
        :disabled="result !== null"
        @click:button="$emit('answer', 'INCORRECT')"
      >
        <Icon name="tabler:circle-x" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Incorrect</span>
      </Button>
    </div>

    <!-- Navigation -->
    <div class="flex justify-between items-center mt-2">
      <Button
        type="navigation"
        size="small"
        button-class="w-28"
        :disabled="currentIndex === 0"
        @click:button="$emit('prev')"
      >
        <Icon name="tabler:chevron-left" class="text-base translate-y-0.5" />
        <span class="font-bold ml-1">Prev</span>
      </Button>
      <Button
        v-if="currentIndex < totalCount - 1"
        type="navigation"
        size="small"
        button-class="w-28"
        :disabled="result === null"
        @click:button="$emit('next')"
      >
        <span class="font-bold mr-1">Next</span>
        <Icon name="tabler:chevron-right" class="text-base translate-y-0.5" />
      </Button>
      <Button
        v-else
        type="navigation"
        size="small"
        button-class="w-36"
        :disabled="!allAnswered"
        @click:button="$emit('complete')"
      >
        <Icon name="tabler:flag-check" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">Complete</span>
      </Button>
    </div>
  </Panel>
</template>

<script setup lang="ts">
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import Flashcard from "~/components/vocabulary/Flashcard.vue";
import type { QuizDirection, QuizResult } from "~/utils/vocabularyQuiz";

defineProps<{
  currentIndex: number;
  totalCount: number;
  flipped: boolean;
  result: QuizResult | null;
  allAnswered: boolean;
  direction: QuizDirection;
  frontText: string;
  backText: string;
  flashcardClass?: string;
  answerButtonsClass?: string;
}>();

defineEmits<{
  (e: "flip" | "prev" | "next" | "complete"): void;
  (e: "answer", result: QuizResult): void;
}>();
</script>
