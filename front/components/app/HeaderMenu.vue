<template>
  <div :class="['container', { closed: !commonStore.isHeaderMenuOpen }]">
    <div class="mail-area">
      <Icon name="tabler:mail" class="adjust-icon" />
      <span class="inline-block ml-2">{{ userStore?.user?.emailAddress }}</span>
    </div>
    <hr class="mt-3" />
    <ul class="mt-3">
      <li>
        <NuxtLink to="/profile/edit" class="menu-item font-cursive">
          <Icon name="tabler:edit" class="menu-icon" />
          Edit Profile
        </NuxtLink>
      </li>
      <li>
        <NuxtLink to="/profile/change-password" class="menu-item font-cursive">
          <Icon name="tabler:lock" class="menu-icon" />
          Change Password
        </NuxtLink>
      </li>
      <li>
        <span class="menu-item font-cursive">
          <Icon name="tabler:logout" class="menu-icon" />
          Logout
        </span>
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

<style scoped>
.container {
  position: fixed;
  top: 4rem;
  right: 1rem;
  z-index: 998;
  width: 16rem;
  background-color: #eee;
  box-shadow: 2px 2px 4px #000;
  padding: 1rem;
  transition: 0.5s;
}

.container.closed {
  transform: translateY(-14rem);
}

.container .mail-area {
  width: stretch;
  padding: 0 1rem;
}

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

.menu-item .menu-icon {
  transform: translate(-1px, 4px);
  font-size: 1.3rem;
}
</style>
