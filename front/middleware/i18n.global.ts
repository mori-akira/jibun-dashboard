export default defineNuxtRouteMiddleware(async () => {
  const { $setLocaleFromApi } = useNuxtApp();
  await $setLocaleFromApi();
});
