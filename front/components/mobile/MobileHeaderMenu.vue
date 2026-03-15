<template>
  <div
    :class="[
      'fixed top-16 right-4 z-[1001] w-40 bg-[#eee] shadow-[2px_2px_4px_#000] p-4 transition duration-300',
      { '-translate-y-[8rem]': !commonStore.isHeaderMenuOpen },
    ]"
  >
    <ul class="list-none p-0">
      <li>
        <NuxtLink
          to="/logout"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:logout"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
          Logout
        </NuxtLink>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
const commonStore = useCommonStore();
</script>
