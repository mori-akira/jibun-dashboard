<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[
          { text: 'Vocabulary', iconName: 'tabler:book', link: '/vocabulary' },
          { text: 'Quiz', iconName: 'tabler:cards' },
        ]"
      />
      <div class="flex items-center mr-4">
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-38"
          @click:button="() => navigateTo('/vocabulary/quiz/history')"
        >
          <Icon name="tabler:history" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">Quiz History</span>
        </Button>
      </div>
    </div>

    <!-- Settings Phase -->
    <template v-if="phase === 'settings'">
      <Panel>
        <h3>
          <Icon name="tabler:settings" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Quiz Settings</span>
        </h3>
        <div class="flex flex-col gap-4 mt-4 ml-4">
          <div class="flex items-center gap-4">
            <span class="font-cursive w-32 shrink-0">Tags</span>
            <MultiOptionSelector
              v-if="(vocabularyStore.vocabularyTags ?? []).length > 0"
              label=""
              :options="(vocabularyStore.vocabularyTags ?? []).map((t: VocabularyTag) => t.vocabularyTag)"
              :values="(vocabularyStore.vocabularyTags ?? []).map((t: VocabularyTag) => t.vocabularyTagId ?? '')"
              :selected-options="selectedTagIds"
              wrapper-class="flex flex-wrap gap-1"
              @click:value="onClickTag"
            />
            <span v-else class="text-gray-500 text-sm"
              >No tags available (all vocabularies in scope)</span
            >
          </div>

          <div class="flex items-center gap-4">
            <span class="font-cursive w-32 shrink-0">Direction</span>
            <div class="flex gap-6">
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  v-model="direction"
                  type="radio"
                  value="FRONT_TO_BACK"
                  class="accent-gray-800"
                />
                <span>Name → Description</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  v-model="direction"
                  type="radio"
                  value="BACK_TO_FRONT"
                  class="accent-gray-800"
                />
                <span>Description → Name</span>
              </label>
            </div>
          </div>

          <div class="flex items-center gap-4">
            <span class="font-cursive w-32 shrink-0">Count</span>
            <SelectBox
              :model-value="String(questionCount)"
              :options="countOptions.map((n: number) => ({ label: String(n), value: String(n) }))"
              :placeholder="
                countOptions.length === 0
                  ? 'No vocabularies in scope'
                  : undefined
              "
              select-wrapper-class="w-32"
              @update:model-value="(v: string) => (questionCount = Number(v))"
            />
            <span class="text-sm text-gray-500"
              >/ {{ scopeCount }} vocabularies in scope</span
            >
          </div>
        </div>
        <div class="flex justify-center mr-4 mt-4">
          <Button
            type="action"
            size="normal"
            button-class="w-40"
            :disabled="countOptions.length === 0 || isLoading"
            @click:button="onStartQuiz"
          >
            <Icon name="tabler:player-play" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Start Quiz</span>
          </Button>
        </div>
      </Panel>
    </template>

    <!-- Quiz Phase -->
    <template v-else-if="phase === 'quiz'">
      <Panel>
        <div class="flex justify-between items-center mb-4">
          <h3>
            <Icon name="tabler:cards" class="adjust-icon-4" />
            <span class="font-cursive font-bold ml-2">Quiz</span>
          </h3>
          <span class="font-cursive text-lg font-bold text-gray-600">
            {{ currentIndex + 1 }} / {{ cards.length }}
          </span>
        </div>

        <!-- Progress bar -->
        <div class="w-full bg-gray-200 rounded-full h-2 mb-6">
          <div
            class="bg-gray-800 h-2 rounded-full transition-all duration-300"
            :style="{ width: `${((currentIndex + 1) / cards.length) * 100}%` }"
          />
        </div>

        <!-- Flashcard -->
        <div class="flex justify-center mb-6">
          <Flashcard
            :key="currentIndex"
            :flipped="currentCard.flipped"
            @click="onFlipCard"
          >
            <template #front>
              <p
                :class="
                  direction === 'FRONT_TO_BACK' ? 'text-center' : 'text-left'
                "
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
                :class="
                  direction === 'FRONT_TO_BACK' ? 'text-left' : 'text-center'
                "
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

        <!-- After Flip -->
        <div v-if="currentCard.flipped" class="flex justify-center gap-6 mb-4">
          <Button
            :type="currentCard.result === 'CORRECT' ? 'action' : 'default'"
            size="normal"
            button-class="w-36"
            :disabled="currentCard.result !== null"
            @click:button="onAnswer('CORRECT')"
          >
            <Icon
              name="tabler:circle-check"
              class="text-base translate-y-0.5"
            />
            <span class="font-bold ml-2">Correct</span>
          </Button>
          <Button
            :type="currentCard.result === 'INCORRECT' ? 'error' : 'default'"
            size="normal"
            button-class="w-36"
            :disabled="currentCard.result !== null"
            @click:button="onAnswer('INCORRECT')"
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
            @click:button="onPrev"
          >
            <Icon
              name="tabler:chevron-left"
              class="text-base translate-y-0.5"
            />
            <span class="font-bold ml-1">Prev</span>
          </Button>
          <Button
            v-if="currentIndex < cards.length - 1"
            type="navigation"
            size="small"
            button-class="w-28"
            :disabled="currentCard.result === null"
            @click:button="onNext"
          >
            <span class="font-bold mr-1">Next</span>
            <Icon
              name="tabler:chevron-right"
              class="text-base translate-y-0.5"
            />
          </Button>
          <Button
            v-else
            type="action"
            size="small"
            button-class="w-36"
            :disabled="!allAnswered"
            @click:button="onComplete"
          >
            <Icon name="tabler:flag-check" class="text-base translate-y-0.5" />
            <span class="font-bold ml-2">Complete</span>
          </Button>
        </div>
      </Panel>
    </template>

    <!-- Complete Phase -->
    <template v-else-if="phase === 'complete'">
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
              <p class="text-4xl font-bold text-gray-800">{{ cards.length }}</p>
              <p class="text-sm font-cursive text-gray-500 mt-1">Total</p>
            </div>
          </div>
          <div class="w-64 bg-gray-200 rounded-full h-3 mt-2">
            <div
              class="bg-gray-800 h-3 rounded-full"
              :style="{ width: `${(correctCount / cards.length) * 100}%` }"
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
          @click:button="onSubmit"
        >
          <Icon name="tabler:send" class="text-base translate-y-0.5" />
          <span class="font-bold ml-2">{{
            submitted ? "Submitted" : "Submit"
          }}</span>
        </Button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type {
  Vocabulary,
  VocabularyTag,
  VocabularyQuizHistoryBaseDirectionEnum,
  VocabularyQuizHistoryAnswerResultEnum,
} from "~/generated/api/client/api";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Button from "~/components/common/Button.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import MultiOptionSelector from "~/components/common/MultiOptionSelector.vue";
import Flashcard from "~/components/vocabulary/Flashcard.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import {
  selectQuizVocabularies,
  getScopeCount,
  getCountOptions,
  type QuizDirection,
  type QuizResult,
} from "~/utils/vocabularyQuiz";

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

type QuizPhase = "settings" | "quiz" | "complete";
type CardState = {
  vocabulary: Vocabulary;
  flipped: boolean;
  result: QuizResult | null;
};

const phase = ref<QuizPhase>("settings");
const selectedTagIds = ref<string[]>([]);
const direction = ref<QuizDirection>("FRONT_TO_BACK");
const questionCount = ref(5);
const cards = ref<CardState[]>([]);
const currentIndex = ref(0);
const submitted = ref(false);

const scopeCount = computed(() =>
  getScopeCount(vocabularyStore.vocabularies ?? [], selectedTagIds.value),
);

const countOptions = computed(() => getCountOptions(scopeCount.value));

watch(countOptions, (opts: number[]) => {
  if (opts.length > 0 && !opts.includes(questionCount.value)) {
    questionCount.value = opts[opts.length - 1]!;
  }
});

const currentCard = computed(() => cards.value[currentIndex.value]!);
const allAnswered = computed(() =>
  cards.value.every((c: CardState) => c.result !== null),
);
const correctCount = computed(
  () => cards.value.filter((c: CardState) => c.result === "CORRECT").length,
);

const frontText = computed(() => {
  const vocab = currentCard.value?.vocabulary;
  return direction.value === "FRONT_TO_BACK"
    ? vocab?.name ?? ""
    : vocab?.description ?? "";
});

const backText = computed(() => {
  const vocab = currentCard.value?.vocabulary;
  return direction.value === "FRONT_TO_BACK"
    ? vocab?.description ?? ""
    : vocab?.name ?? "";
});

onMounted(async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        vocabularyStore.fetchVocabularies(),
        vocabularyStore.fetchVocabularyTags(),
        vocabularyStore.fetchQuizHistories(),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});

function onClickTag(tagId: string) {
  const idx = selectedTagIds.value.indexOf(tagId);
  if (idx >= 0) selectedTagIds.value.splice(idx, 1);
  else selectedTagIds.value.push(tagId);
}

function onStartQuiz() {
  const selected = selectQuizVocabularies(
    vocabularyStore.vocabularies ?? [],
    vocabularyStore.quizHistories ?? [],
    selectedTagIds.value,
    questionCount.value,
    direction.value,
  );
  cards.value = selected.map((v) => ({
    vocabulary: v,
    flipped: false,
    result: null,
  }));
  currentIndex.value = 0;
  submitted.value = false;
  phase.value = "quiz";
}

function onFlipCard() {
  if (currentCard.value) {
    currentCard.value.flipped = true;
  }
}

function onAnswer(result: QuizResult) {
  if (currentCard.value && currentCard.value.result === null) {
    currentCard.value.result = result;
  }
}

function onNext() {
  if (currentIndex.value < cards.value.length - 1) {
    currentIndex.value++;
  }
}

function onPrev() {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
}

function onComplete() {
  phase.value = "complete";
}

async function onSubmit() {
  if (submitted.value) return;
  await withLoading(async () => {
    try {
      await vocabularyStore.postQuizHistory({
        tagIds:
          selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined,
        questionCount: cards.value.length,
        direction: direction.value as VocabularyQuizHistoryBaseDirectionEnum,
        answers: cards.value.map((c: CardState) => ({
          vocabularyId: c.vocabulary.vocabularyId!,
          result: c.result as VocabularyQuizHistoryAnswerResultEnum,
        })),
      });
      submitted.value = true;
      await vocabularyStore.fetchQuizHistories();
      await navigateTo("/vocabulary/quiz/history");
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
}
</script>
