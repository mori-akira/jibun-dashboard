import type {
  Vocabulary,
  VocabularyQuizHistoryBaseDirectionEnum,
  VocabularyQuizHistoryAnswerResultEnum,
} from "~/generated/api/client/api";
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

export type QuizPhase = "settings" | "quiz" | "complete";
export type CardState = {
  vocabulary: Vocabulary;
  flipped: boolean;
  result: QuizResult | null;
};

export function useVocabularyQuiz(historyPath = "/vocabulary/quiz/history") {
  const commonStore = useCommonStore();
  const vocabularyStore = useVocabularyStore();
  const { isLoading, withLoading } = useLoadingQueue();

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

  async function init() {
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
  }

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
        await navigateTo(historyPath);
      } catch (err) {
        console.error(err);
        commonStore.addErrorMessage(getErrorMessage(err));
      }
    });
  }

  return {
    // state
    phase,
    selectedTagIds,
    direction,
    questionCount,
    cards,
    currentIndex,
    submitted,
    isLoading,
    // computed
    scopeCount,
    countOptions,
    currentCard,
    allAnswered,
    correctCount,
    frontText,
    backText,
    // methods
    init,
    onClickTag,
    onStartQuiz,
    onFlipCard,
    onAnswer,
    onNext,
    onPrev,
    onComplete,
    onSubmit,
  };
}
