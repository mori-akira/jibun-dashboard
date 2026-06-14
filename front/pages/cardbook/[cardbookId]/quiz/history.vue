<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Cardbook', iconName: 'tabler:books', link: '/cardbook' },
        {
          text: cardbookName,
          iconName: 'tabler:book',
          link: `/cardbook/${cardbookId}`,
        },
        {
          text: 'Quiz',
          iconName: 'tabler:cards',
          link: `/cardbook/${cardbookId}/quiz`,
        },
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
import QuizHistoryPanel from "~/components/cardbook/quiz/QuizHistoryPanel.vue";
import { useCommonStore } from "~/stores/common";
import { useCardbookStore } from "~/stores/cardbook";
import { useLoadingQueue } from "~/composables/common/useLoadingQueue";
import { getErrorMessage } from "~/utils/error";

const route = useRoute();
const cardbookId = route.params.cardbookId as string;

const commonStore = useCommonStore();
const cardbookStore = useCardbookStore();
const { isLoading, withLoading } = useLoadingQueue();

const cardbookName = computed(
  () =>
    cardbookStore.cardbooks?.find((c) => c.cardbookId === cardbookId)?.name ??
    cardbookId,
);

onMounted(async () => {
  await withLoading(async () => {
    try {
      await Promise.all([
        cardbookStore.fetchCardbooks(),
        cardbookStore.fetchCardbookCards(cardbookId),
        cardbookStore.fetchQuizHistories(cardbookId),
      ]);
    } catch (err) {
      console.error(err);
      commonStore.addErrorMessage(getErrorMessage(err));
    }
  });
});
</script>
