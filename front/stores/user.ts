import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/generated/api/client/configuration";
import type { User, Password } from "~/generated/api/client/api";
import { UserApi } from "~/generated/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useUserStore = defineStore("user", () => {
  const user = ref<User | null>(null);
  const password = ref<Password | null>(null);

  const { getAccessToken } = useAuth();
  const getUserApi = () => {
    const configuration = new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
    return new UserApi(configuration);
  };

  async function fetchUser() {
    const res = await getUserApi().getUser();
    user.value = res.data;
  }

  async function putUser(newUser: User) {
    await getUserApi().putUser(newUser);
  }

  async function postPassword(newPassword: Password) {
    await getUserApi().postPassword(newPassword);
  }

  return {
    user,
    password,
    fetchUser,
    putUser,
    postPassword,
  };
});
