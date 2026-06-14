<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Cardbook', iconName: 'tabler:books', link: '/m/cardbook' },
        {
          text: breadcrumbName,
          iconName: 'tabler:book',
          link: `/m/cardbook/${cardbookId}`,
        },
        {
          text: 'Quiz',
          iconName: 'tabler:cards',
          link: `/m/cardbook/${cardbookId}/quiz`,
        },
        { text: 'Quiz History', iconName: 'tabler:history' },
      ]"
    />

    <QuizHistoryPanel
      :is-loading="isLoading"
      panel-wrapper-class="w-full overflow-x-auto mt-4"
      table-wrapper-class="min-w-96 flex justify-center mt-4 mx-1"
      :visible-fields="['answeredAtFormatted', 'questionCount', 'correctCount']"
      modal-box-class="w-[85vw] max-h-[80vh] overflow-y-auto"
      summary-wrapper-class="flex flex-col items-center w-full"
      summary-label-class="bg-gray-800 text-white w-32 font-cursive"
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

definePageMeta({ layout: "mobile" });

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
const breadcrumbName = computed(() =>
  cardbookName.value.length > 8
    ? cardbookName.value.slice(0, 8) + "…"
    : cardbookName.value,
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
