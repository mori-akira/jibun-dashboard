<template>
  <div>
    <h2>
      <Icon name="tabler:edit" class="title-icon" />
      <span class="font-cursive font-bold ml-2">Edit Profile</span>
    </h2>

    <Panel>
      <TextBox
        label="Name"
        :value="user?.userName || ''"
        @blur:value="
          (value) => {
            if (user) {
              user.userName = value;
            }
          }
        "
      />
      <TextBox
        label="Email"
        :value="user?.emailAddress || ''"
        @blur:value="
          (value) => {
            if (user) {
              user.emailAddress = value;
            }
          }
        "
      />
    </Panel>
  </div>
</template>

<script setup lang="ts">
import type { User } from "~/api/client/api";
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";
import { useUserStore } from "~/stores/user";

const userStore = useUserStore();
const user = ref<User | null>({ ...userStore.user } as User);
</script>

<style scoped></style>
