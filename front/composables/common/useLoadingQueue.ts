import { ref, computed } from "vue";
import { generateRandomString } from "~/utils/rand";

export const useLoadingQueue = () => {
  const queue = ref<string[]>([]);
  const isLoading = computed(() => queue.value.length > 0);

  const withLoading = async (fn: () => Promise<void>) => {
    const id = generateRandomString();
    queue.value.push(id);
    try {
      await fn();
    } finally {
      queue.value = queue.value.filter((e) => e !== id);
    }
  };

  return { isLoading, withLoading };
};
