import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/api/client/configuration";
import type { Setting } from "~/api/client/api";
import { SettingApi } from "~/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useSettingStore = defineStore("setting", () => {
  const setting = ref<Setting | null>(null);

  const { getAccessToken } = useAuth();
  const getSettingApi = () => {
    const configuration = new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
    return new SettingApi(configuration);
  };

  async function fetchSetting() {
    const res = await getSettingApi().getSetting();
    setting.value = res.data;
  }

  async function putSetting(newSetting: Setting) {
    await getSettingApi().putSetting(newSetting);
    await fetchSetting();
  }

  return {
    setting,
    fetchSetting,
    putSetting,
  };
});
