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
      commonStore.setLoading(true);
      const res = await settingApi.getSetting();
      setting.value = res.data;
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.setLoading(false);
    }
  }

  async function putSetting(newSetting: Setting) {
    try {
      commonStore.setLoading(true);
      await settingApi.putSetting(newSetting);
      await fetchSetting();
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.setLoading(false);
    }
  }

  return {
    setting,
    fetchSetting,
    putSetting,
  };
});
