<template>
  <nav
    :class="[
      'w-64 bg-gray-100 pt-16 px-4 border-r-4 border-[#666] min-h-screen transition duration-500',
      { 'w-[13px] p-0': !commonStore.isNavOpen },
    ]"
  >
    <div
      :class="['toggle-area', { closed: !commonStore.isNavOpen }]"
      @click="commonStore.toggleNav"
    >
      <Icon
        name="tabler:caret-left-filled"
        :class="[
          'text-[1.2rem] text-[#333] translate-x-[-1px] translate-y-[-1px]',
          { 'scale-x-[-1]': !commonStore.isNavOpen },
        ]"
      />
    </div>
    <div
      :class="[
        'w-56 transition duration-500',
        { '-translate-x-56': !commonStore.isNavOpen },
      ]"
    >
      <ul class="list-none p-0">
        <li>
          <NuxtLink
            class="inline-block w-full py-[0.2rem] px-4 mb-[15px] rounded-lg no-underline font-bold text-gray-900 hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
            :class="{ active: isActive('/') }"
            to="/"
          >
            <Icon
              name="tabler:home"
              class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
            />
            <span class="font-cursive ml-2">Home</span>
          </NuxtLink>
        </li>
        <li>
          <NuxtLink
            class="inline-block w-full py-[0.2rem] px-4 mb-[15px] rounded-lg no-underline font-bold text-gray-900 hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
            :class="{ active: isActive('/salary') }"
            to="/salary"
          >
            <Icon
              name="tabler:report-money"
              class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
            />
            <span class="font-cursive ml-2">Salary</span>
          </NuxtLink>
        </li>
        <li>
          <NuxtLink
            class="inline-block w-full py-[0.2rem] px-4 mb-[15px] rounded-lg no-underline font-bold text-gray-900 hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
            :class="{ active: isActive('/qualification') }"
            to="/qualification"
          >
            <Icon
              name="tabler:certificate"
              class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
            />
            <span class="font-cursive ml-2">Qualification</span>
          </NuxtLink>
        </li>
        <li>
          <NuxtLink
            class="inline-block w-full py-[0.2rem] px-4 mb-[15px] rounded-lg no-underline font-bold text-gray-900 hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
            :class="{ active: isActive('/vocabulary') }"
            to="/vocabulary"
          >
            <Icon
              name="tabler:book"
              class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
            />
            <span class="font-cursive ml-2">Vocabulary</span>
          </NuxtLink>
        </li>
        <li>
          <NuxtLink
            class="inline-block w-full py-[0.2rem] px-4 mb-[15px] rounded-lg no-underline font-bold text-gray-900 hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
            :class="{ active: isActive('/financial-asset') }"
            to="/financial-asset"
          >
            <Icon
              name="tabler:report-money"
              class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
            />
            <span class="font-cursive ml-2">Financial Asset</span>
          </NuxtLink>
        </li>
        <li>
          <NuxtLink
            class="inline-block w-full py-[0.2rem] px-4 mb-[15px] rounded-lg no-underline font-bold text-gray-900 hover:bg-black/20 hover:cursor-pointer hover:underline [&.active]:bg-black/20 [&.active]:underline [&.active]:hover:cursor-auto"
            :class="{ active: isActive('/study-plan') }"
            to="/study-plan"
          >
            <Icon
              name="tabler:file-pencil"
              class="translate-x-[-1px] translate-y-[4px] text-[1.3rem]"
            />
            <span class="font-cursive ml-2">Study Plan</span>
          </NuxtLink>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useRoute } from "#app";
import { useCommonStore } from "~/stores/common";

const route = useRoute();
const commonStore = useCommonStore();
const isActive = (base: string) =>
  base === "/"
    ? route.path === base || route.path === ""
    : route.path.startsWith(base);
</script>

<style lang="css" scoped>
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
  cursor: pointer;
}

.toggle-area:hover {
  background-color: #ddd;
}

.toggle-area.closed {
  left: 0px;
}
</style>
