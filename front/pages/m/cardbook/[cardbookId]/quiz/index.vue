<template>
  <div>
    <div class="flex justify-between items-center mr-2">
      <Breadcrumb
        :items="[
          { text: 'Cardbook', iconName: 'tabler:books', link: '/m/cardbook' },
          {
            text: breadcrumbName,
            iconName: 'tabler:book',
            link: `/m/cardbook/${cardbookId}`,
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
        @click:button="() => navigateTo(`/m/cardbook/${cardbookId}/quiz/history`)"
      >
        <Icon name="tabler:history" class="text-base translate-y-0.5" />
        <span class="font-bold ml-2">History</span>
      </Button>
    </div>

    <!-- Settings Phase -->
    <template v-if="phase === 'settings'">
      <QuizSettings
        :direction="direction"
        :question-count="questionCount"
        :count-options="countOptions"
        :scope-count="scopeCount"
        :is-loading="isLoading"
        layout="vertical"
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
import QuizSettings from "~/components/cardbook/quiz/QuizSettings.vue";
import QuizCard from "~/components/cardbook/quiz/QuizCard.vue";
import QuizResult from "~/components/cardbook/quiz/QuizResult.vue";
import { useCardbookStore } from "~/stores/cardbook";
import { useCardbookQuiz } from "~/composables/cardbook/useCardbookQuiz";

definePageMeta({ layout: "mobile" });

const route = useRoute();
const cardbookId = route.params.cardbookId as string;

const cardbookStore = useCardbookStore();
const cardbookName = computed(
  () =>
    cardbookStore.cardbooks?.find((c) => c.cardbookId === cardbookId)?.name ??
    cardbookId,
);
const breadcrumbName = computed(() =>
  cardbookName.value.length > 8
    ? cardbookName.value.slice(0, 8) + "…"
    : cardbookName.value,
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
} = useCardbookQuiz(cardbookId, `/m/cardbook/${cardbookId}/quiz/history`);

onMounted(init);
</script>
