import tailwindcss from "@tailwindcss/vite";

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2025-01-01",
  devtools: { enabled: true },
  css: ["~/assets/css/main.css"],
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@nuxt/icon",
    "@nuxt/test-utils/module",
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
    "nuxt-codemirror",
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
    head: {
      title: "Jibun Dashboard",
      viewport: "width=device-width",
      link: [
        {
          rel: "icon",
          type: "image/x-icon",
          href: "/favicon.ico",
        },
      ],
    },
  },
  build: {
    transpile: ["@vuepic/vue-datepicker"],
  },
  nuxtCodemirror: {
    basicSetup: true,
  },
  vite: {
    plugins: [
      tailwindcss(),
      {
        name: "vite-plugin-ignore-sourcemap-warnings",
        apply: "build",
        configResolved(config) {
          config.build.rollupOptions.onwarn = (warning, warn) => {
            if (
              warning.code === "SOURCEMAP_BROKEN" &&
              warning.plugin === "@tailwindcss/vite:generate:build"
            ) {
              return;
            }

            warn(warning);
          };
        },
      },
    ],
    server: {
      watch: {
        usePolling: true,
      },
    },
  },
});
