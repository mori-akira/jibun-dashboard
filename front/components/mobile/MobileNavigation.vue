<template>
  <div
    :class="[
      'fixed top-[4rem] left-2 z-[1001] w-54 bg-[#eee] shadow-[2px_2px_4px_#000] p-4 transition duration-300',
      { '-translate-y-[12rem]': !commonStore.isNavOpen },
    ]"
  >
    <ul class="list-none p-0">
      <li>
        <NuxtLink
          to="/m"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:home"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
          Home
        </NuxtLink>
      </li>
      <li class="mt-2">
        <NuxtLink
          to="/m/salary"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:report-money"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
          Salary
        </NuxtLink>
      </li>
      <li class="mt-2">
        <NuxtLink
          to="/m/qualification"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:certificate"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
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
