import { Configuration } from "~/api/client/configuration";
import { ResourceApi } from "~/api/client";
import { useAuth } from "~/composables/common/useAuth";

type Dict = Record<string, string>;

export default defineNuxtPlugin((nuxtApp) => {
  const cache = useState<Record<string, Dict>>("i18nCache", () => ({}));

  const normalizePlaceholders = (s: string) =>
    s.replace(/\$\{(\w+)\}/g, "{$1}");

  const fetchMessages = async (locale: string): Promise<Dict> => {
    if (cache.value[locale]) {
      return cache.value[locale];
    }

    const { getAccessToken } = useAuth();
    const configuration = new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
    const resourceApi = new ResourceApi(configuration);

    const data = (await resourceApi.getI18n(locale)).data;
    // 値の正規化
    const normalized: Dict = {};
    for (const [k, v] of Object.entries(data)) {
      normalized[k] = normalizePlaceholders(v ?? "");
    }
    cache.value[locale] = normalized;
    return normalized;
  };

  // i18n へ注入するユーティリティ
  const ensureLocaleLoaded = async (locale: "en") => {
    const { setLocaleMessage, availableLocales } = useI18n();
    if (!availableLocales.includes(locale)) {
      const messages = await fetchMessages(locale);
      setLocaleMessage(locale, messages);
    }
  };

  // アプリ起動時（SSR含む）に現在ロケールをロード
  nuxtApp.hook("app:created", async () => {
    const { locale } = useI18n();
    await ensureLocaleLoaded(locale.value);
  });

  // ルート戦略でロケールが切り替わる場合に備えてミドルウェアで確実にロード
  addRouteMiddleware(
    "i18n-api-loader",
    async (to) => {
      // /en/... のように prefix がある場合は to.params.locale（設定に依存）
      const routeLocale =
        (to.params?.locale as "en" | undefined) ?? useI18n().locale.value;
      await ensureLocaleLoaded(routeLocale);
    },
    { global: true }
  );

  // 言語切替用のヘルパを提供（UIの言語スイッチャから使う）
  return {
    provide: {
      setLocaleFromApi: async (locale: "en") => {
        const { setLocale } = useI18n();
        await ensureLocaleLoaded(locale);
        await setLocale(locale);
      },
    },
  };
});
