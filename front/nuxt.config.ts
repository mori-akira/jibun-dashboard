// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  // compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  css: ["~/asset/css/main.css"],

  modules: [
    "@nuxt/content",
    "@nuxt/fonts",
    // '@nuxt/icon',
    "@nuxt/image",
    "@nuxt/scripts",
    "@nuxt/test-utils",
    "@nuxt/ui",
  ],
});
