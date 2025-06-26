import { defineStore } from "pinia";
import { ref } from "vue";

export const useCommonStore = defineStore("common", () => {
  const isNavOpen = ref(true);

  function toggleNav() {
    isNavOpen.value = !isNavOpen.value;
  }

  function setNavOpen(value: boolean) {
    isNavOpen.value = value;
  }

  return {
    isNavOpen,
    toggleNav,
    setNavOpen,
  };
});
