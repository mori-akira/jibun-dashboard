import { defineStore } from "pinia";
import { ref } from "vue";
import type { User, Password } from "~/api/client/api";
import { UserApi } from "~/api/client/api";

const userApi = new UserApi();

export const useUserStore = defineStore("user", () => {
  const user = ref<User | null>(null);
  const password = ref<Password | null>(null);

  async function fetchUser() {
    try {
      const res = await userApi.getUser();
      user.value = res.data;
    } catch (error) {
      console.error("Failed to fetch user:", error);
    }
  }

  return {
    user,
    password,
    fetchUser,
  };
});
