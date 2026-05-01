<template>
  <div>
    <div class="flex justify-between items-center mr-2">
      <Breadcrumb
        :items="[
          {
            text: 'Vocabulary',
            iconName: 'tabler:book',
            link: '/m/vocabulary',
          },
          { text: 'Quiz', iconName: 'tabler:cards' },
        ]"
      />
    </div>
    <div class="flex justify-end items-center mt-2 mr-2">
      <Button
        v-if="phase === 'settings'"
        type="navigation"
        size="small"
        html-type="button"
        button-class="w-38"
        @click:button="() => navigateTo('/m/vocabulary/quiz/history')"
      >
        <Icon name="tabler:history" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">History</span>
      </Button>
    </div>

    <!-- Settings Phase -->
    <template v-if="phase === 'settings'">
      <QuizSettings
        :vocabulary-tags="vocabularyStore.vocabularyTags ?? []"
        :selected-tag-ids="selectedTagIds"
        :direction="direction"
        :question-count="questionCount"
        :count-options="countOptions"
        :scope-count="scopeCount"
        :is-loading="isLoading"
        layout="vertical"
        @click:tag="onClickTag"
        @update:direction="direction = $event"
        @update:question-count="questionCount = $event"
        @start="onStartQuiz"
      />
    </template>

    <!-- Quiz Phase -->
    <template v-else-if="phase === 'quiz'">
      <QuizCard
        :current-index="currentIndex"
        :total-count="cards.length"
        :flipped="currentCard.flipped"
        :result="currentCard.result"
        :all-answered="allAnswered"
        :direction="direction"
        :front-text="frontText"
        :back-text="backText"
        flashcard-class="w-full"
        answer-buttons-class="flex-col items-center"
        @flip="onFlipCard"
        @answer="onAnswer"
        @prev="onPrev"
        @next="onNext"
        @complete="onComplete"
      />
    </template>

    <!-- Complete Phase -->
    <template v-else-if="phase === 'complete'">
      <QuizResult
        :correct-count="correctCount"
        :total-count="cards.length"
        :submitted="submitted"
        :is-loading="isLoading"
        @submit="onSubmit"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Button from "~/components/common/Button.vue";
import QuizSettings from "~/components/vocabulary/quiz/QuizSettings.vue";
import QuizCard from "~/components/vocabulary/quiz/QuizCard.vue";
import QuizResult from "~/components/vocabulary/quiz/QuizResult.vue";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useVocabularyQuiz } from "~/composables/vocabulary/useVocabularyQuiz";

definePageMeta({ layout: "mobile" });

const vocabularyStore = useVocabularyStore();
const {
  phase,
  selectedTagIds,
  direction,
  questionCount,
  cards,
  currentIndex,
  submitted,
  isLoading,
  scopeCount,
  countOptions,
  currentCard,
  allAnswered,
  correctCount,
  frontText,
  backText,
  init,
  onClickTag,
  onStartQuiz,
  onFlipCard,
  onAnswer,
  onNext,
  onPrev,
  onComplete,
  onSubmit,
} = useVocabularyQuiz("/m/vocabulary/quiz/history");

onMounted(init);
</script>
