<template>
  <div>
    <Breadcrumb
      :items="[{ text: 'Study Plan', iconName: 'tabler:file-pencil' }]"
    />
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
</script>
