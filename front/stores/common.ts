import { defineStore } from "pinia";
import { ref } from "vue";

export const useCommonStore = defineStore("common", () => {
  const isNavOpen = ref(true);
  const isHeaderMenuOpen = ref(false);

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

  return {
    isNavOpen,
    isHeaderMenuOpen,
    toggleNav,
    setNavOpen,
    toggleHeaderMenu,
    setHeaderMenuOpen,
  };
});
