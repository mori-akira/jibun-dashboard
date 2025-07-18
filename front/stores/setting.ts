import { defineStore } from "pinia";
import { ref } from "vue";

import type { Setting } from "~/api/client/api";
import { SettingApi } from "~/api/client/api";
import { useCommonStore } from "~/stores/common";
import { getErrorMessage } from "~/utils/error";

export const useSettingStore = defineStore("setting", () => {
  const settingApi = new SettingApi();
  const commonStore = useCommonStore();
  const setting = ref<Setting | null>(null);

  async function fetchSetting() {
    try {
      const res = await settingApi.getSetting();
      setting.value = res.data;
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    }
  }

  async function putSetting(newSetting: Setting) {
    try {
      await settingApi.putSetting(newSetting);
      await fetchSetting();
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    }
  }

  return {
    setting,
    fetchSetting,
    putSetting,
  };
});
