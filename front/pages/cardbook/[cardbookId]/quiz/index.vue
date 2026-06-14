<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[
          { text: 'Cardbook', iconName: 'tabler:books', link: '/cardbook' },
          {
            text: cardbookName,
            iconName: 'tabler:book',
            link: `/cardbook/${cardbookId}`,
          },
          { text: 'Quiz', iconName: 'tabler:cards' },
        ]"
      />
      <div class="flex items-center mr-4">
        <Button
          v-if="phase === 'settings'"
          type="navigation"
          size="small"
          button-class="w-38"
          @click:button="() => navigateTo(`/cardbook/${cardbookId}/quiz/history`)"
        >
          <Icon name="tabler:history" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Quiz History</span>
        </Button>
      </div>
    </div>

    <!-- Settings Phase -->
    <template v-if="phase === 'settings'">
      <QuizSettings
        :direction="direction"
        :question-count="questionCount"
        :count-options="countOptions"
        :scope-count="scopeCount"
        :is-loading="isLoading"
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
        flashcard-class="w-160"
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
import QuizSettings from "~/components/cardbook/quiz/QuizSettings.vue";
import QuizCard from "~/components/cardbook/quiz/QuizCard.vue";
import QuizResult from "~/components/cardbook/quiz/QuizResult.vue";
import { useCardbookStore } from "~/stores/cardbook";
import { useCardbookQuiz } from "~/composables/cardbook/useCardbookQuiz";

const route = useRoute();
const cardbookId = route.params.cardbookId as string;

const cardbookStore = useCardbookStore();
const cardbookName = computed(
  () =>
    cardbookStore.cardbooks?.find((c) => c.cardbookId === cardbookId)?.name ??
    cardbookId,
);

const {
  phase,
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
  onStartQuiz,
  onFlipCard,
  onAnswer,
  onNext,
  onPrev,
  onComplete,
  onSubmit,
} = useCardbookQuiz(cardbookId);

onMounted(init);
</script>
