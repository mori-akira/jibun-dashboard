import { defineStore } from "pinia";
import { ref } from "vue";

import type { Setting } from "~/api/client/api";
import { SettingApi } from "~/api/client/api";

export const useSettingStore = defineStore("setting", () => {
  const settingApi = new SettingApi();
  const setting = ref<Setting | null>(null);

  async function fetchSetting() {
    const res = await settingApi.getSetting();
    setting.value = res.data;
  }

  async function putSetting(newSetting: Setting) {
    await settingApi.putSetting(newSetting);
    await fetchSetting();
  }

  return {
    setting,
    fetchSetting,
    putSetting,
  };
});
