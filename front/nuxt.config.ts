import tailwindcss from "@tailwindcss/vite";

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2025-01-01",
  devtools: { enabled: true },
  css: ["~/asset/css/main.css"],
  modules: ["@nuxt/content", "@nuxt/eslint", "@nuxt/ui", "@nuxt/icon"],
  vite: {
    plugins: [tailwindcss()],
    server: {
      watch: {
        usePolling: true,
      },
    },
  },
  runtimeConfig: {
    public: {
      apiBaseUrl: process.env.NUXT_PUBLIC_API_BASE_URL || "",
      apiMode: process.env.NUXT_PUBLIC_API_MODE || "default",
    },
  },
});
