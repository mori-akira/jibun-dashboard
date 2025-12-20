import { defineStore } from "pinia";
import { ref } from "vue";

import type { Setting } from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useSettingStore = defineStore("setting", () => {
  const setting = ref<Setting | null>(null);
  const { getSettingApi } = useApiClient();

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
