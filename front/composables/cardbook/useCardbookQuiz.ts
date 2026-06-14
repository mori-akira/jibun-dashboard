import type {
  CardbookCard,
  CardbookQuizHistoryBaseDirectionEnum,
  CardbookQuizHistoryAnswerResultEnum,
} from "~/generated/api/client/api";
import { useCommonStore } from "~/stores/common";
import { useCardbookStore } from "~/stores/cardbook";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";
import { selectQuizCards, getCardbookScopeCount } from "~/utils/cardbookQuiz";
import {
  getCountOptions,
  type QuizDirection,
  type QuizResult,
} from "~/utils/vocabularyQuiz";

export type QuizPhase = "settings" | "quiz" | "complete";
export type CardState = {
  card: CardbookCard;
  flipped: boolean;
  result: QuizResult | null;
};

export function useCardbookQuiz(cardbookId: string) {
  const commonStore = useCommonStore();
  const cardbookStore = useCardbookStore();
  const { isLoading, withLoading } = useLoadingQueue();

  const historyPath = `/cardbook/${cardbookId}/quiz/history`;

  const phase = ref<QuizPhase>("settings");
  const direction = ref<QuizDirection>("FRONT_TO_BACK");
  const questionCount = ref(5);
  const cards = ref<CardState[]>([]);
  const currentIndex = ref(0);
  const submitted = ref(false);

  const scopeCount = computed(() =>
    getCardbookScopeCount(cardbookStore.cardbookCards ?? []),
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
    const card = currentCard.value?.card;
    return direction.value === "FRONT_TO_BACK"
      ? card?.front ?? ""
      : card?.back ?? "";
  });

  const backText = computed(() => {
    const card = currentCard.value?.card;
    return direction.value === "FRONT_TO_BACK"
      ? card?.back ?? ""
      : card?.front ?? "";
  });

  async function init() {
    await withLoading(async () => {
      try {
        await Promise.all([
          cardbookStore.fetchCardbookCards(cardbookId),
          cardbookStore.fetchQuizHistories(cardbookId),
        ]);
      } catch (err) {
        console.error(err);
        commonStore.addErrorMessage(getErrorMessage(err));
      }
    });
  }

  function onStartQuiz() {
    const selected = selectQuizCards(
      cardbookStore.cardbookCards ?? [],
      cardbookStore.quizHistories ?? [],
      questionCount.value,
      direction.value,
    );
    cards.value = selected.map((c) => ({
      card: c,
      flipped: false,
      result: null,
    }));
    currentIndex.value = 0;
    submitted.value = false;
    phase.value = "quiz";
  }

  function onFlipCard() {
    if (currentCard.value) {
      currentCard.value.flipped = !currentCard.value.flipped;
    }
  }

  function onAnswer(result: QuizResult) {
    if (currentCard.value) {
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
        await cardbookStore.postQuizHistory({
          cardbookId,
          questionCount: cards.value.length,
          direction: direction.value as CardbookQuizHistoryBaseDirectionEnum,
          answers: cards.value.map((c: CardState) => ({
            cardId: c.card.cardId!,
            result: c.result as CardbookQuizHistoryAnswerResultEnum,
          })),
        });
        submitted.value = true;
        await cardbookStore.fetchQuizHistories(cardbookId);
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
    onStartQuiz,
    onFlipCard,
    onAnswer,
    onNext,
    onPrev,
    onComplete,
    onSubmit,
  };
}
