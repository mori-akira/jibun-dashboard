<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Vocabulary', iconName: 'tabler:book', link: '/m/vocabulary' },
        { text: 'Quiz', iconName: 'tabler:cards', link: '/m/vocabulary/quiz' },
        { text: 'Quiz History', iconName: 'tabler:history' },
      ]"
    />

    <QuizHistoryPanel
      :is-loading="isLoading"
      panel-wrapper-class="w-full overflow-x-auto mt-4"
      table-wrapper-class="min-w-96 flex justify-center mt-4 mx-1"
      :visible-fields="[
        'answeredAtFormatted',
        'tagNames',
        'questionCount',
        'correctCount',
      ]"
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

definePageMeta({ layout: "mobile" });

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();
const { isLoading, withLoading } = useLoadingQueue();

onMounted(async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        vocabularyStore.fetchQuizHistories(),
        vocabularyStore.fetchVocabularyTags(),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});
</script>
