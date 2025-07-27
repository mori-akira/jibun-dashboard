import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/api/client/configuration";
import type { User, Password } from "~/api/client/api";
import { UserApi } from "~/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useUserStore = defineStore("user", () => {
  const { getAccessToken } = useAuth();
  const configuration = new Configuration({
    baseOptions: {
      headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
    },
  });
  const userApi = new UserApi(configuration);
  const user = ref<User | null>(null);
  const password = ref<Password | null>(null);

  async function fetchUser() {
    const res = await userApi.getUser();
    user.value = res.data;
  }

  async function putUser(newUser: User) {
    await userApi.putUser(newUser);
    await fetchUser();
  }

  async function postPassword(newPassword: Password) {
    await userApi.postPassword(newPassword);
  }

  return {
    user,
    password,
    fetchUser,
    putUser,
    postPassword,
  };
});
