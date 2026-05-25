<template>
  <span :class="['badge', `badge-${type}`]">
    <slot />
  </span>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    type?: 'primary' | 'success' | 'warning' | 'danger';
  }>(),
  {
    type: 'primary',
  }
);
</script>
