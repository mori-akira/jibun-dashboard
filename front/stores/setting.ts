import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/generated/api/client/configuration";
import type { Setting } from "~/generated/api/client/api";
import { SettingApi } from "~/generated/api/client/api";
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
    const res = await getSettingApi().getSettings();
    setting.value = res.data;
  }

  async function putSetting(newSetting: Setting) {
    await getSettingApi().putSettings(newSetting);
  }

  return {
    setting,
    fetchSetting,
    putSetting,
  };
});
