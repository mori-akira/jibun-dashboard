import { defineStore } from "pinia";
import { ref } from "vue";

export const useCommonStore = defineStore("common", () => {
  const isNavOpen = ref<boolean>(true);
  const isHeaderMenuOpen = ref<boolean>(false);
  const hasUnsavedChange = ref<boolean>(false);
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

  function setHasUnsavedChange(value: boolean) {
    hasUnsavedChange.value = value;
  }

  function addLoadingQueue(id?: string): string {
    let newId = "";
    if (id) {
      newId = id;
    } else {
      newId = generateRandomString();
    }
    loadingQueue.value = [...loadingQueue.value, newId];
    return newId;
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
    hasUnsavedChange,
    loadingQueue,
    errorMessages,
    toggleNav,
    setNavOpen,
    toggleHeaderMenu,
    setHeaderMenuOpen,
    setHasUnsavedChange,
    addLoadingQueue,
    deleteLoadingQueue,
    addErrorMessage,
    addErrorMessages,
    clearErrorMessages,
  };
});
