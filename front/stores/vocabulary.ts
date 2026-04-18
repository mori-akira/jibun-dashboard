import { defineStore } from "pinia";
import { ref } from "vue";

import type {
  Vocabulary,
  VocabularyRequest,
  VocabularyTag,
  VocabularyTagBase,
} from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useVocabularyStore = defineStore("vocabulary", () => {
  const vocabularies = ref<Vocabulary[] | null>(null);
  const vocabularyTags = ref<VocabularyTag[] | null>(null);
  const { getVocabularyApi } = useApiClient();

  async function fetchVocabularies(
    name?: string,
    description?: string,
    tags?: string[],
  ) {
    const res = await getVocabularyApi().getVocabularies(
      name || undefined,
      description || undefined,
      tags?.length ? tags : undefined,
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

  async function postVocabularyTag(vocabularyTag: VocabularyTagBase) {
    await getVocabularyApi().postVocabularyTags(vocabularyTag);
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

  return {
    vocabularies,
    vocabularyTags,
    fetchVocabularies,
    postVocabulary,
    putVocabulary,
    deleteVocabulary,
    fetchVocabularyTags,
    postVocabularyTag,
    putVocabularyTag,
    deleteVocabularyTag,
  };
});
