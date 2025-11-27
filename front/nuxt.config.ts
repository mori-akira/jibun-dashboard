import tailwindcss from "@tailwindcss/vite";
import urlJoin from "url-join";

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2025-01-01",
  srcDir: ".",
  devtools: { enabled: true },
  ssr: false,
  css: ["~/assets/css/main.css"],
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@nuxt/icon",
    "@nuxt/test-utils/module",
    "@nuxtjs/i18n",
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
    "@vite-pwa/nuxt",
  ],
  i18n: {
    strategy: "prefix_except_default",
    defaultLocale: "en",
    locales: [{ code: "en", name: "English" }],
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: "i18n_redirected",
      redirectOn: "root",
    },
    vueI18n: "app/i18n.config.ts",
  },
  runtimeConfig: {
    public: {
      baseUrl: process.env.NUXT_PUBLIC_BASE_URL || "/",
      apiBaseUrl: process.env.NUXT_PUBLIC_API_BASE_URL || "/",
      apiMode: process.env.NUXT_PUBLIC_API_MODE || "default",
      requireAuth: ["true", "on"].includes(
        process.env.NUXT_PUBLIC_REQUIRE_AUTH?.toLowerCase() || "off"
      ),
      region: process.env.NUXT_PUBLIC_REGION || "",
      cognitoClientId: process.env.NUXT_PUBLIC_COGNITO_CLIENT_ID || "",
      cognitoDomain: process.env.NUXT_PUBLIC_COGNITO_DOMAIN || "",
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
          href: urlJoin(process.env.NUXT_PUBLIC_BASE_URL || "/", "favicon.ico"),
        },
      ],
    },
  },
  build: {
    transpile: ["@vuepic/vue-datepicker", "form-data"],
  },
  nuxtCodemirror: {
    basicSetup: true,
  },
  pwa: {
    registerType: "autoUpdate",
    manifest: {
      name: "Jibun Dashboard",
      short_name: "Jibun",
      description:
        "Personal dashboard for managing salary, qualifications, and more",
      theme_color: "#eeffff",
      background_color: "#eeffff",
      display: "standalone",
      orientation: "portrait",
      scope: "/",
      start_url: "/",
      icons: [
        {
          src: "pwa-192x192.png",
          sizes: "192x192",
          type: "image/png",
        },
        {
          src: "pwa-512x512.png",
          sizes: "512x512",
          type: "image/png",
        },
        {
          src: "pwa-512x512.png",
          sizes: "512x512",
          type: "image/png",
          purpose: "any maskable",
        },
      ],
    },
    workbox: {
      navigateFallback: "/",
      globPatterns: ["**/*.{js,css,html,png,svg,ico}"],
      runtimeCaching: [
        {
          urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
          handler: "CacheFirst",
          options: {
            cacheName: "google-fonts-cache",
            expiration: {
              maxEntries: 10,
              maxAgeSeconds: 60 * 60 * 24 * 365, // 1 year
            },
            cacheableResponse: {
              statuses: [0, 200],
            },
          },
        },
      ],
    },
    client: {
      installPrompt: true,
      periodicSyncForUpdates: 3600, // Check for updates every hour
    },
    devOptions: {
      enabled: true,
      type: "module",
    },
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
