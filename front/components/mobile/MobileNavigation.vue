<template>
  <div
    :class="[
      'fixed top-[-8rem] left-2 z-[1001] w-54 bg-[#eee] shadow-[2px_2px_4px_#000] p-4 transition duration-500',
      { '-translate-y-[-12rem]': commonStore.isNavOpen },
    ]"
  >
    <ul class="list-none p-0">
      <li>
        <NuxtLink to="/m" class="menu-item font-cursive" active-class="active">
          <Icon name="tabler:home" class="menu-icon" />
          Home
        </NuxtLink>
      </li>
      <li class="mt-2">
        <NuxtLink
          to="/m/salary"
          class="menu-item font-cursive"
          active-class="active"
        >
          <Icon name="tabler:report-money" class="menu-icon" />
          Salary
        </NuxtLink>
      </li>
      <li class="mt-2">
        <NuxtLink
          to="/m/qualification"
          class="menu-item font-cursive"
          active-class="active"
        >
          <Icon name="tabler:certificate" class="menu-icon" />
          Qualification
        </NuxtLink>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";

const commonStore = useCommonStore();
</script>

<style lang="css" scoped>
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
