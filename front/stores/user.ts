import { defineStore } from "pinia";
import { ref } from "vue";

import type { User, Password, UserBase } from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useUserStore = defineStore("user", () => {
  const user = ref<User | null>(null);
  const password = ref<Password | null>(null);
  const { getUserApi } = useApiClient();

  async function fetchUser() {
    const res = await getUserApi().getUsers();
    user.value = res.data;
  }

  async function putUser(newUser: UserBase) {
    await getUserApi().putUsers(newUser);
  }

  async function postPassword(newPassword: Password) {
    await getUserApi().putPassword(newPassword);
  }

  return {
    user,
    password,
    fetchUser,
    putUser,
    postPassword,
  };
});
