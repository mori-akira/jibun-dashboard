import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/api/client/configuration";
import type { Setting } from "~/api/client/api";
import { SettingApi } from "~/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useSettingStore = defineStore("setting", () => {
  const { getAccessToken } = useAuth();
  const configuration = new Configuration({
    baseOptions: {
      headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
    },
  });
  const settingApi = new SettingApi(configuration);
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
