import type { Composer } from "vue-i18n";

export const i18nT = (key: string, named?: Record<string, unknown>) => {
  const composer = useNuxtApp().$i18n as Composer;
  return named ? composer.t(key, named) : composer.t(key);
};
