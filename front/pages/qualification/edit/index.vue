<template>
  <div>
    <Breadcrumb
      :items="[
        {
          text: 'Qualification',
          iconName: 'tabler:certificate',
          link: '/qualification',
        },
        { text: 'Edit', iconName: 'tabler:database-edit' },
      ]"
    />
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
</script>
