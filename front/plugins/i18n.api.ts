import type { Composer } from "vue-i18n";
import { Configuration } from "~/api/client/configuration";
import { ResourceApi } from "~/api/client";
import { useAuth } from "~/composables/common/useAuth";

export default defineNuxtPlugin((nuxtApp) => {
  const cache = useState<Record<string, Record<string, string>>>(
    "i18nCache",
    () => ({})
  );

  const fetchMessages = async (
    locale: string
  ): Promise<Record<string, string>> => {
    if (cache.value[locale]) {
      return cache.value[locale];
    }

    const { getAccessToken } = useAuth();
    const configuration = new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
    const api = new ResourceApi(configuration);
    const data = (await api.getI18n(locale)).data;

    const messages: Record<string, string> = {};
    for (const [k, v] of Object.entries(data)) {
      if (k === "localeCode") continue;
      messages[k] = String(v ?? "");
    }
    cache.value[locale] = messages;
    return messages;
  };

  const ensureLocaleLoaded = async (locale: "en") => {
    const i18n = nuxtApp.$i18n as Composer;
    const current = i18n.getLocaleMessage(locale) as
      | Record<string, unknown>
      | undefined;

    if (!current || Object.keys(current).length === 0) {
      const messages = await fetchMessages(locale);
      i18n.setLocaleMessage(locale, messages);
    } else if (!cache.value[locale]) {
      cache.value[locale] = Object.fromEntries(
        Object.entries(current).map(([k, v]) => [k, String(v ?? "")])
      );
    }
  };

  return {
    provide: {
      setLocaleFromApi: async (locale: "en" = "en") => {
        const i18n = nuxtApp.$i18n as Composer;
        await ensureLocaleLoaded(locale);
        i18n.locale.value = locale;
      },
    },
  };
});
