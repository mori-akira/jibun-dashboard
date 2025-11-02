<template>
  <div
    :class="[
      'fixed top-16 right-4 z-[998] w-64 bg-[#eee] shadow-[2px_2px_4px_#000] p-4 transition duration-500',
      { '-translate-y-[17rem]': !commonStore.isHeaderMenuOpen },
    ]"
  >
    <div class="w-full px-4">
      <Icon name="tabler:mail" class="adjust-icon-4" />
      <span class="inline-block ml-2">{{ userStore?.user?.emailAddress }}</span>
    </div>
    <hr class="mt-3" />
    <ul class="mt-3">
      <li>
        <NuxtLink
          to="/profile/edit"
          class="menu-item font-cursive"
          active-class="active"
        >
          <Icon name="tabler:edit" class="menu-icon" />
          Edit Profile
        </NuxtLink>
      </li>
      <li>
        <NuxtLink
          to="/profile/change-password"
          class="menu-item font-cursive"
          active-class="active"
        >
          <Icon name="tabler:lock" class="menu-icon" />
          Change Password
        </NuxtLink>
      </li>
      <li>
        <NuxtLink
          to="/profile/setting"
          class="menu-item font-cursive"
          active-class="active"
        >
          <Icon name="tabler:settings" class="menu-icon" />
          Setting
        </NuxtLink>
      </li>
      <li>
        <NuxtLink
          to="/logout"
          class="menu-item font-cursive"
          active-class="active"
        >
          <Icon name="tabler:logout" class="menu-icon" />
          Logout
        </NuxtLink>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";

const commonStore = useCommonStore();
const userStore = useUserStore();
</script>

<style lang="css" scoped>
ul {
  list-style-type: none;
  padding: 0;
}

li:nth-child(n + 2) {
  margin-top: 0.5rem;
}

.menu-item {
  display: inline-block;
  width: stretch;
  padding: 0.2rem 1rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: bold;
}

.menu-item:hover {
  background-color: #00000033;
  cursor: pointer;
  text-decoration: underline;
}

.menu-item.active {
  background-color: #00000033;
  text-decoration: underline;
}

.menu-item.active:hover {
  cursor: unset;
}

.menu-item .menu-icon {
  transform: translate(-1px, 4px);
  font-size: 1.3rem;
}
</style>
