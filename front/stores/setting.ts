import { defineStore } from "pinia";
import { ref } from "vue";

import type { Setting } from "~/api/client/api";
import { SettingApi } from "~/api/client/api";
import { useCommonStore } from "~/stores/common";
import { getErrorMessage } from "~/utils/error";
import { generateRandomString } from "~/utils/rand";

export const useSettingStore = defineStore("setting", () => {
  const settingApi = new SettingApi();
  const commonStore = useCommonStore();
  const setting = ref<Setting | null>(null);

  async function fetchSetting() {
    const id = generateRandomString();
    try {
      commonStore.addLoadingQueue(id);
      const res = await settingApi.getSetting();
      setting.value = res.data;
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.deleteLoadingQueue(id);
    }
  }

  async function putSetting(newSetting: Setting) {
    const id = commonStore.addLoadingQueue();
    try {
      await settingApi.putSetting(newSetting);
      await fetchSetting();
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.deleteLoadingQueue(id);
    }
  }

  return {
    setting,
    fetchSetting,
    putSetting,
  };
});
