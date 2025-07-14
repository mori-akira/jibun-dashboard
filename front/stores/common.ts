import { defineStore } from "pinia";
import { ref } from "vue";

export const useCommonStore = defineStore("common", () => {
  const isNavOpen = ref(true);
  const isHeaderMenuOpen = ref(false);
  const loadingQueue = ref<string[]>([]);
  const errorMessages = ref<string[]>([]);

  function toggleNav() {
    isNavOpen.value = !isNavOpen.value;
  }

  function setNavOpen(value: boolean) {
    isNavOpen.value = value;
  }

  function toggleHeaderMenu() {
    isHeaderMenuOpen.value = !isHeaderMenuOpen.value;
  }

  function setHeaderMenuOpen(value: boolean) {
    isHeaderMenuOpen.value = value;
  }

  function addLoadingQueue(id: string) {
    loadingQueue.value = [...loadingQueue.value, id];
  }

  function deleteLoadingQueue(id: string) {
    loadingQueue.value = loadingQueue.value.filter((e) => e !== id);
  }

  function addErrorMessage(message: string) {
    errorMessages.value.push(message);
  }

  function addErrorMessages(messages: string[]) {
    messages.forEach((message) => errorMessages.value.push(message));
  }

  function clearErrorMessages() {
    errorMessages.value = [];
  }

  return {
    isNavOpen,
    isHeaderMenuOpen,
    loadingQueue,
    errorMessages,
    toggleNav,
    setNavOpen,
    toggleHeaderMenu,
    setHeaderMenuOpen,
    addLoadingQueue,
    deleteLoadingQueue,
    addErrorMessage,
    addErrorMessages,
    clearErrorMessages,
  };
});
