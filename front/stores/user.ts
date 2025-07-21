import { defineStore } from "pinia";
import { ref } from "vue";

import type { User, Password } from "~/api/client/api";
import { UserApi } from "~/api/client/api";

export const useUserStore = defineStore("user", () => {
  const userApi = new UserApi();
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
