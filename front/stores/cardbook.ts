import { defineStore } from "pinia";
import { ref } from "vue";

import type {
  Cardbook,
  CardbookBase,
  CardbookCard,
  CardbookCardBase,
  CardbookCheckResult,
  CardbookCheckResultStatus,
  CardbookQuizHistory,
  CardbookQuizHistoryBase,
  GetCardbookCheckResultsSeveritiesEnum,
  GetCardbookCheckResultsStatusesEnum,
} from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useCardbookStore = defineStore("cardbook", () => {
  const cardbooks = ref<Cardbook[] | null>(null);
  const cardbookCards = ref<CardbookCard[] | null>(null);
  const quizHistories = ref<CardbookQuizHistory[] | null>(null);
  const checkResults = ref<CardbookCheckResult[] | null>(null);
  const cardCountMap = ref<Map<string, number>>(new Map());
  const { getCardbookApi } = useApiClient();

  async function fetchCardbooks() {
    const res = await getCardbookApi().getCardbooks();
    cardbooks.value = res.data;
  }

  async function postCardbook(base: CardbookBase): Promise<string | undefined> {
    const res = await getCardbookApi().postCardbooks(base);
    return res.data.cardbookId;
  }

  async function putCardbook(cardbookId: string, base: CardbookBase) {
    await getCardbookApi().putCardbooksById(cardbookId, base);
  }

  async function deleteCardbook(cardbookId: string) {
    await getCardbookApi().deleteCardbooksById(cardbookId);
  }

  async function fetchCardbookCards(cardbookId: string) {
    const res = await getCardbookApi().getCardbookCards(cardbookId);
    cardbookCards.value = res.data;
  }

  async function fetchCardCountMap(cardbookIds: string[]) {
    const entries = await Promise.all(
      cardbookIds.map(async (id) => {
        const res = await getCardbookApi().getCardbookCards(id);
        return [id, res.data.length] as [string, number];
      }),
    );
    cardCountMap.value = new Map(entries);
  }

  async function postCardbookCard(
    cardbookId: string,
    base: CardbookCardBase,
  ): Promise<string | undefined> {
    const res = await getCardbookApi().postCardbookCards(cardbookId, base);
    return res.data.cardId;
  }

  async function putCardbookCard(
    cardbookId: string,
    cardId: string,
    base: CardbookCardBase,
  ) {
    await getCardbookApi().putCardbookCardsById(cardbookId, cardId, base);
  }

  async function deleteCardbookCard(cardbookId: string, cardId: string) {
    await getCardbookApi().deleteCardbookCardsById(cardbookId, cardId);
  }

  async function fetchQuizHistories(cardbookId?: string) {
    const res = await getCardbookApi().getCardbookQuizHistories(
      cardbookId || undefined,
    );
    quizHistories.value = res.data;
  }

  async function postQuizHistory(
    base: CardbookQuizHistoryBase,
  ): Promise<string | undefined> {
    const res = await getCardbookApi().postCardbookQuizHistories(base);
    return res.data.quizHistoryId;
  }

  async function deleteQuizHistory(quizHistoryId: string) {
    await getCardbookApi().deleteCardbookQuizHistoriesById(quizHistoryId);
  }

  async function fetchCheckResults(
    cardbookId?: string,
    checkedAtFrom?: string,
    checkedAtTo?: string,
    severities?: string[],
    statuses?: string[],
  ) {
    const res = await getCardbookApi().getCardbookCheckResults(
      cardbookId || undefined,
      checkedAtFrom || undefined,
      checkedAtTo || undefined,
      severities?.length
        ? (severities as GetCardbookCheckResultsSeveritiesEnum[])
        : undefined,
      statuses?.length
        ? (statuses as GetCardbookCheckResultsStatusesEnum[])
        : undefined,
    );
    checkResults.value = res.data;
  }

  async function updateCheckResultStatus(
    checkResultId: string,
    status: CardbookCheckResultStatus,
  ) {
    await getCardbookApi().putCardbookCheckResultStatusById(
      checkResultId,
      status,
    );
  }

  return {
    cardbooks,
    cardbookCards,
    quizHistories,
    checkResults,
    cardCountMap,
    fetchCardbooks,
    fetchCardCountMap,
    postCardbook,
    putCardbook,
    deleteCardbook,
    fetchCardbookCards,
    postCardbookCard,
    putCardbookCard,
    deleteCardbookCard,
    fetchQuizHistories,
    postQuizHistory,
    deleteQuizHistory,
    fetchCheckResults,
    updateCheckResultStatus,
  };
});
