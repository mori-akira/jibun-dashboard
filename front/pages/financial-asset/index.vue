<template>
  <div>
    <Breadcrumb
      :items="[{ text: 'Financial Asset', iconName: 'tabler:report-money' }]"
    />
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
</script>
