import { defineStore } from "pinia";
import { ref } from "vue";

import type { User, Password } from "~/api/client/api";
import { UserApi } from "~/api/client/api";
import { useCommonStore } from "~/stores/common";
import { getErrorMessage } from "~/utils/error";
import { generateRandomString } from "~/utils/rand";

export const useUserStore = defineStore("user", () => {
  const userApi = new UserApi();
  const commonStore = useCommonStore();
  const user = ref<User | null>(null);
  const password = ref<Password | null>(null);

  async function fetchUser() {
    const id = generateRandomString();
    try {
      commonStore.addLoadingQueue(id);
      const res = await userApi.getUser();
      user.value = res.data;
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.deleteLoadingQueue(id);
    }
  }

  async function putUser(newUser: User) {
    const id = generateRandomString();
    try {
      commonStore.addLoadingQueue(id);
      await userApi.putUser(newUser);
      await fetchUser();
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.deleteLoadingQueue(id);
    }
  }

  async function postPassword(newPassword: Password) {
    const id = generateRandomString();
    try {
      commonStore.addLoadingQueue(id);
      await userApi.postPassword(newPassword);
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.deleteLoadingQueue(id);
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
