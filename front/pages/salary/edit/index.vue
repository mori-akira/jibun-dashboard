<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Salary', iconName: 'tabler:report-money', link: '/salary' },
        { text: 'Edit', iconName: 'tabler:database-edit' },
      ]"
    />
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
</script>
