<template>
  <div
    :class="[
      'fixed top-16 right-4 z-[998] w-64 bg-[#eee] shadow-[2px_2px_4px_#000] p-4 transition duration-500',
      { '-translate-y-[17rem]': !commonStore.isHeaderMenuOpen },
    ]"
  >
    <div class="w-full px-4 whitespace-nowrap">
      <Icon name="tabler:mail" class="adjust-icon-4" />
      <span class="inline-block ml-2">{{ userStore?.user?.emailAddress }}</span>
    </div>
    <hr class="mt-3" />
    <ul class="mt-3 list-none p-0">
      <li>
        <NuxtLink
          to="/profile/edit"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:edit"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
          Edit Profile
        </NuxtLink>
      </li>
      <li class="mt-2">
        <NuxtLink
          to="/profile/change-password"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:lock"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
          Change Password
        </NuxtLink>
      </li>
      <li class="mt-2">
        <NuxtLink
          to="/profile/setting"
          class="inline-block w-full py-[0.2rem] px-4 rounded-lg no-underline font-bold font-cursive hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
          active-class="active"
        >
          <Icon
            name="tabler:settings"
            class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
          />
          Setting
        </NuxtLink>
      </li>
      <li class="mt-2">
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
import { useUserStore } from "~/stores/user";

const commonStore = useCommonStore();
const userStore = useUserStore();
</script>
