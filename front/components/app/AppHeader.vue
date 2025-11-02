<template>
  <header
    class="fixed flex justify-between z-[999] pt-[0.8rem] pb-[0.2rem] items-center w-screen bg-white border-b-2 border-[#666] translate-y-[-8px]"
  >
    <div
      :class="[
        'toggle-area relative w-[30px] h-[30px] ml-3 cursor-pointer',
        { opened: commonStore.isNavOpen },
      ]"
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
        data-testid="app-header-title"
        >Jibun Dashboard</NuxtLink
      >
    </div>
    <div class="flex items-center mr-4">
      <span class="inline-block mr-2">{{ userName }}</span>
      <div
        class="flex items-center p-1 cursor-pointer hover:bg-gray-100 hover:rounded-full"
        @click="commonStore.toggleHeaderMenu"
      >
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

.toggle-menu-icon.open {
  transform: rotate(-90deg);
}
</style>
