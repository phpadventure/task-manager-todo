<template>
  <div :class="containerClasses">
    <div :class="spinnerClasses" />
    <p v-if="label" class="mt-2 text-sm text-gray-600">{{ label }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    size?: 'sm' | 'md' | 'lg';
    label?: string;
  }>(),
  {
    size: 'md',
  }
);

const containerClasses = computed(() => {
  const base = 'flex flex-col items-center justify-center';
  const sizes: Record<string, string> = {
    sm: 'gap-1',
    md: 'gap-2',
    lg: 'gap-3',
  };
  return `${base} ${sizes[props.size]}`;
});

const spinnerClasses = computed(() => {
  const base = 'border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin';
  const sizes: Record<string, string> = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };
  return `${base} ${sizes[props.size]}`;
});
</script>
