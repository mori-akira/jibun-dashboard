<template>
  <header class="w-screen bg-white">
    <div
      :class="['toggle-area', { opened: commonStore.isNavOpen }]"
      @click="commonStore.toggleNav"
    >
      <span class="line bg-gray-900" />
      <span class="line bg-gray-900" />
      <span class="line bg-gray-900" />
    </div>
    <div class="title-area">
      <NuxtLink
        to="/"
        class="font-sacramento text-3xl text-gray-900 font-bold"
        data-test-id="app-header-title"
        >Jibun Dashboard</NuxtLink
      >
    </div>
    <div class="user-area">
      <span class="user-name">{{ userName }}</span>
      <div class="toggle-menu-wrapper" @click="commonStore.toggleHeaderMenu">
        <Icon
          name="tabler:caret-left-filled"
          :class="['toggle-menu-icon', { open: commonStore.isHeaderMenuOpen }]"
        />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";

const commonStore = useCommonStore();
const userStore = useUserStore();
const userName = computed(() => userStore.user?.userName ?? "");
</script>

<style lang="css" scoped>
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: fixed;
  z-index: 999;
  padding: 0.8rem 0 0.2rem;
  border-bottom: #666 2px solid;
  transform: translateY(-8px);
}

.toggle-area {
  position: relative;
  width: 30px;
  height: 30px;
  margin-left: 12px;
}

.toggle-area:hover {
  cursor: pointer;
}

.toggle-area .line {
  position: absolute;
  display: inline-block;
  top: 12px;
  width: 30px;
  height: 4px;
  transition: 0.5s;
}

.toggle-area:hover .line {
  background-color: #666;
}

.toggle-area .line:nth-child(1) {
  top: 3px;
}

.toggle-area .line:nth-child(3) {
  top: 21px;
}

.toggle-area.opened .line:nth-child(1) {
  transform: rotate(-45deg) translateY(12px);
}

.toggle-area.opened .line:nth-child(2) {
  opacity: 0;
}

.toggle-area.opened .line:nth-child(3) {
  transform: rotate(45deg) translateY(-12px);
}

.user-area {
  display: flex;
  align-items: center;
  margin-right: 1rem;
}

.user-area .user-name {
  display: inline-block;
  margin-right: 0.5rem;
}

.toggle-menu-wrapper {
  display: flex;
  align-items: center;
  padding: 4px;
}

.toggle-menu-wrapper:hover {
  cursor: pointer;
  background-color: #eee;
  border-radius: 50%;
}

.toggle-menu-icon.open {
  transform: rotate(-90deg);
}
</style>
