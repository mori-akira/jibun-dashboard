import { defineStore } from "pinia";
import { ref } from "vue";

export const useCommonStore = defineStore("common", () => {
  const isNavOpen = ref(true);
  const isHeaderMenuOpen = ref(false);
  const isLoading = ref(false);
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

  function setLoading(value: boolean) {
    isLoading.value = value;
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
    isLoading,
    errorMessages,
    toggleNav,
    setNavOpen,
    toggleHeaderMenu,
    setHeaderMenuOpen,
    setLoading,
    addErrorMessage,
    addErrorMessages,
    clearErrorMessages,
  };
});
