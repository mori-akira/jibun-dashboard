export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.hook("page:finish", () => {
    document.querySelector("main")?.scrollTo({ left: 0, top: 0 });
  });
});
