<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Vocabulary', iconName: 'tabler:book', link: '/vocabulary' },
        { text: 'Quiz', iconName: 'tabler:cards', link: '/vocabulary/quiz' },
        { text: 'Quiz History', iconName: 'tabler:history' },
      ]"
    />

    <QuizHistoryPanel
      :is-loading="isLoading"
      panel-wrapper-class="overflow-x-auto"
      table-wrapper-class="min-w-192 flex justify-center mt-4 ml-10 mr-10"
      modal-box-class="w-[40rem] max-h-[80vh] overflow-y-auto"
      summary-wrapper-class="flex flex-col items-center w-full"
      summary-label-class="bg-gray-800 text-white w-48 font-cursive"
    />
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import QuizHistoryPanel from "~/components/vocabulary/quiz/QuizHistoryPanel.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

onMounted(async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        vocabularyStore.fetchQuizHistories(),
        vocabularyStore.fetchVocabularyTags(),
        vocabularyStore.fetchVocabularies(),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});
</script>
