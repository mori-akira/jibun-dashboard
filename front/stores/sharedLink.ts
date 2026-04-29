import { defineStore } from "pinia";
import { ref } from "vue";

import type { SharedLink, SharedLinkBase } from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useSharedLinkStore = defineStore("sharedLink", () => {
  const sharedLinks = ref<SharedLink[] | null>(null);
  const { getSharedLinkApi } = useApiClient();

  async function fetchSharedLinks() {
    const res = await getSharedLinkApi().getSharedLinks();
    sharedLinks.value = res.data;
  }

  async function postSharedLink(sharedLink: SharedLinkBase) {
    await getSharedLinkApi().postSharedLinks(sharedLink);
  }

  async function deleteSharedLink(token: string) {
    await getSharedLinkApi().deleteSharedLinksById(token);
  }

  return {
    sharedLinks,
    fetchSharedLinks,
    postSharedLink,
    deleteSharedLink,
  };
});
