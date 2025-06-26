<template>
  <nav :class="clsx({ closed: !commonStore.isNavOpen })">
    <div
      :class="clsx('toggle-area', { closed: !commonStore.isNavOpen })"
      @click="commonStore.toggleNav"
    >
      <Icon name="tabler:caret-left-filled" class="toggle-icon" />
    </div>
    <div :class="clsx('content-area', { closed: !commonStore.isNavOpen })">
      <ul>
        <li>
          <NuxtLink class="text-gray-900" to="/">
            <Icon name="tabler:home-filled" class="nav-icon" />
            <span class="font-cursive ml-2">home</span>
          </NuxtLink>
        </li>
        <li>
          <NuxtLink class="text-gray-900" to="/salary">
            <Icon name="tabler:report-money" class="nav-icon" />
            <span class="font-cursive ml-2">salary</span>
          </NuxtLink>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
import { clsx } from "clsx";

const commonStore = useCommonStore();
</script>

<style scoped>
nav {
  width: 16rem;
  background-color: #eee;
  border-right: #666 4px solid;
  padding: 4rem 1rem 0;
  min-height: 100vh;
  transition: 0.5s;
}

nav.closed {
  width: 13px;
  padding: 0;
}

ul {
  list-style-type: none;
  padding: 0;
}

a {
  display: inline-block;
  width: 100%;
  padding: 0.2rem 1rem;
  margin-bottom: 15px;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: bold;
}

a:hover {
  background-color: #00000033;
  cursor: pointer;
  text-decoration: underline;
}

.toggle-area {
  position: fixed;
  top: calc(50vh - 1rem);
  left: calc(16rem - 0.8rem);
  width: 1.4rem;
  height: 1.4rem;
  background-color: #fff;
  border: #666 3px solid;
  border-radius: 50%;
  transition: 0.5s;
}

.toggle-area:hover {
  cursor: pointer;
  background-color: #ddd;
}

.toggle-area.closed {
  left: 0px;
}

.toggle-area.closed .toggle-icon {
  transform: translate(-1px, -1px) scaleX(-1);
}

.toggle-area .toggle-icon {
  font-size: 1.2rem;
  color: #333;
  transform: translate(-1px, -1px);
}

.content-area {
  width: 14rem;
  transition: 0.5s;
}

.content-area.closed {
  transform: translateX(-14rem);
}

.nav-icon {
  transform: translate(-1px, 2px);
}
</style>
