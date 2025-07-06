import tailwindcss from "@tailwindcss/vite";

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2025-01-01",
  devtools: { enabled: true },
  css: ["~/asset/css/main.css"],
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@nuxt/icon",
    [
      "@vee-validate/nuxt",
      {
        // disable or enable auto imports
        autoImports: true,
        // Use different names for components
        componentNames: {
          Form: "VeeForm",
          Field: "VeeField",
          FieldArray: "VeeFieldArray",
          ErrorMessage: "VeeErrorMessage",
        },
      },
    ],
  ],
  runtimeConfig: {
    public: {
      baseUrl: process.env.NUXT_PUBLIC_BASE_URL || "/",
      apiBaseUrl: process.env.NUXT_PUBLIC_API_BASE_URL || "/",
      apiMode: process.env.NUXT_PUBLIC_API_MODE || "default",
    },
  },
  app: {
    baseURL: process.env.NUXT_PUBLIC_BASE_URL || "/",
  },
  build: {
    transpile: ["@vuepic/vue-datepicker"],
  },
  vite: {
    plugins: [tailwindcss()],
    server: {
      watch: {
        usePolling: true,
      },
    },
  },
});
