import { defineStore } from "pinia";
import { ref } from "vue";

import type {
  Vocabulary,
  VocabularyRequest,
  VocabularyTag,
  VocabularyTagBase,
  VocabularyQuizHistory,
  VocabularyQuizHistoryBase,
  VocabularyCheckResult,
  VocabularyCheckResultStatus,
} from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useVocabularyStore = defineStore("vocabulary", () => {
  const vocabularies = ref<Vocabulary[] | null>(null);
  const vocabularyTags = ref<VocabularyTag[] | null>(null);
  const quizHistories = ref<VocabularyQuizHistory[] | null>(null);
  const checkResults = ref<VocabularyCheckResult[] | null>(null);
  const { getVocabularyApi } = useApiClient();

  async function fetchVocabularies(
    name?: string,
    description?: string,
    tagIds?: string[],
  ) {
    const res = await getVocabularyApi().getVocabularies(
      name || undefined,
      description || undefined,
      tagIds?.length ? tagIds : undefined,
    );
    vocabularies.value = res.data;
  }

  async function postVocabulary(vocabulary: VocabularyRequest) {
    await getVocabularyApi().postVocabularies(vocabulary);
  }

  async function putVocabulary(
    vocabularyId: string | undefined,
    vocabulary: VocabularyRequest,
  ) {
    if (vocabularyId) {
      await getVocabularyApi().putVocabulariesById(vocabularyId, vocabulary);
    } else {
      await postVocabulary(vocabulary);
    }
  }

  async function deleteVocabulary(vocabularyId: string) {
    await getVocabularyApi().deleteVocabulariesById(vocabularyId);
  }

  async function fetchVocabularyTags(tag?: string) {
    const res = await getVocabularyApi().getVocabularyTags(tag || undefined);
    vocabularyTags.value = res.data;
  }

  async function postVocabularyTag(
    vocabularyTag: VocabularyTagBase,
  ): Promise<string | undefined> {
    const res = await getVocabularyApi().postVocabularyTags(vocabularyTag);
    return res.data.vocabularyTagId;
  }

  async function putVocabularyTag(
    vocabularyTagId: string | undefined,
    vocabularyTag: VocabularyTagBase,
  ) {
    if (vocabularyTagId) {
      await getVocabularyApi().putVocabularyTagsById(
        vocabularyTagId,
        vocabularyTag,
      );
    } else {
      await postVocabularyTag(vocabularyTag);
    }
  }

  async function deleteVocabularyTag(vocabularyTagId: string) {
    await getVocabularyApi().deleteVocabularyTagsById(vocabularyTagId);
  }

  async function fetchQuizHistories() {
    const res = await getVocabularyApi().getVocabularyQuizHistories();
    quizHistories.value = res.data;
  }

  async function postQuizHistory(
    base: VocabularyQuizHistoryBase,
  ): Promise<string | undefined> {
    const res = await getVocabularyApi().postVocabularyQuizHistories(base);
    return res.data.quizHistoryId;
  }

  async function deleteQuizHistory(quizHistoryId: string) {
    await getVocabularyApi().deleteVocabularyQuizHistoriesById(quizHistoryId);
  }

  async function fetchCheckResults() {
    const res = await getVocabularyApi().getVocabularyCheckResults();
    checkResults.value = res.data;
  }

  async function updateCheckResultStatus(
    checkResultId: string,
    status: VocabularyCheckResultStatus,
  ) {
    await getVocabularyApi().putVocabularyCheckResultStatusById(
      checkResultId,
      status,
    );
  }

  return {
    vocabularies,
    vocabularyTags,
    quizHistories,
    checkResults,
    fetchVocabularies,
    postVocabulary,
    putVocabulary,
    deleteVocabulary,
    fetchVocabularyTags,
    postVocabularyTag,
    putVocabularyTag,
    deleteVocabularyTag,
    fetchQuizHistories,
    postQuizHistory,
    deleteQuizHistory,
    fetchCheckResults,
    updateCheckResultStatus,
  };
});
