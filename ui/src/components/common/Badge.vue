<template>
  <span :class="badgeClasses">
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'badge-info' | 'badge-error';
  }>(),
  {
    type: 'primary',
  }
);

const badgeClasses = computed(() => {
  const baseClasses =
    'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap';
  const typeClasses: Record<string, string> = {
    primary: 'bg-blue-100 text-blue-800',
    success: 'bg-green-100 text-green-800',
    warning: 'bg-amber-100 text-amber-800',
    danger: 'bg-red-100 text-red-800',
    'badge-info': 'bg-blue-100 text-blue-800',
    'badge-error': 'bg-red-100 text-red-800',
  };
  return `${baseClasses} ${typeClasses[props.type] || typeClasses.primary}`;
});
</script>
