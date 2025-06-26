<template>
  <header class="w-screen bg-white">
    <div
      :class="clsx('toggle-area', { opened: commonStore.isNavOpen })"
      @click="commonStore.toggleNav"
    >
      <span class="line bg-gray-900" />
      <span class="line bg-gray-900" />
      <span class="line bg-gray-900" />
    </div>
    <div class="title-area">
      <span class="font-sacramento text-3xl text-gray-900 font-bold"
        >Jibun Dashboard</span
      >
    </div>
    <div class="icon-area"></div>
  </header>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
import { clsx } from "clsx";

const commonStore = useCommonStore();
</script>

<style scoped>
header {
  display: flex;
  justify-content: space-between;
  position: fixed;
  z-index: 9999;
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
  top: 15px;
  width: 30px;
  height: 4px;
  transition: 0.5s;
}

.toggle-area:hover .line {
  background-color: #666;
}

.toggle-area .line:nth-child(1) {
  top: 6px;
}

.toggle-area .line:nth-child(3) {
  top: 24px;
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
