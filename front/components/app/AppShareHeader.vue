<template>
  <header
    class="fixed flex justify-between z-[999] pt-[0.8rem] pb-[0.2rem] items-center w-screen bg-white border-b-2 border-[#666] translate-y-[-8px]"
  >
    <div
      :class="[
        'toggle-area group relative w-[30px] h-[30px] ml-3 cursor-pointer',
        { opened: commonStore.isNavOpen },
      ]"
      @click="commonStore.toggleNav"
    >
      <span class="line bg-gray-900 group-hover:bg-[#666]" />
      <span class="line bg-gray-900 group-hover:bg-[#666]" />
      <span class="line bg-gray-900 group-hover:bg-[#666]" />
    </div>
    <div class="title-area">
      <span class="font-sacramento text-3xl text-gray-900 font-bold"
        >Jibun Dashboard</span
      >
    </div>
    <div class="w-[30px] mr-4" />
  </header>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";

const commonStore = useCommonStore();
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
</style>
