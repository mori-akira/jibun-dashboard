import { defineStore } from "pinia";
import { ref } from "vue";

import type { User, Password } from "~/api/client/api";
import { UserApi } from "~/api/client/api";
import { useCommonStore } from "~/stores/common";
import { getErrorMessage } from "~/util/error";

const userApi = new UserApi();
const commonStore = useCommonStore();

export const useUserStore = defineStore("user", () => {
  const user = ref<User | null>(null);
  const password = ref<Password | null>(null);

  async function fetchUser() {
    try {
      const res = await userApi.getUser();
      user.value = res.data;
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    }
  }

  async function putUser(newUser: User) {
    try {
      commonStore.setLoading(true);
      await userApi.putUser(newUser);
      await fetchUser();
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.setLoading(false);
    }
  }

  async function postPassword(newPassword: Password) {
    try {
      commonStore.setLoading(true);
      await userApi.postPassword(newPassword);
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.setLoading(false);
    }
  }

  return {
    user,
    password,
    fetchUser,
    putUser,
    postPassword,
  };
});
