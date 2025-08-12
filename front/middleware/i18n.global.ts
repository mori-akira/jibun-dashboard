export default defineNuxtRouteMiddleware(async () => {
  const { $setLocaleFromApi } = useNuxtApp();
  try {
    await $setLocaleFromApi();
  } catch (err) {
    if (import.meta.dev) {
      console.error(err);
    }
  }
});
