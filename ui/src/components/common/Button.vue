<template>
  <button :type="type" :disabled="disabled || loading" :class="buttonClasses" v-bind="$attrs">
    <span
      v-if="loading"
      class="inline-block w-4 h-4 mr-2 border-2 border-current border-t-transparent rounded-full animate-spin"
    />
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    type?: 'button' | 'submit' | 'reset';
    variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
    size?: 'sm' | 'md' | 'lg';
    disabled?: boolean;
    loading?: boolean;
  }>(),
  {
    type: 'button',
    variant: 'primary',
    size: 'md',
    disabled: false,
    loading: false,
  }
);

const buttonClasses = computed(() => {
  const base =
    'font-medium rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed';

  const variants: Record<string, string> = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800',
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 active:bg-gray-400',
    danger: 'bg-red-600 text-white hover:bg-red-700 active:bg-red-800',
    ghost: 'text-gray-700 hover:bg-gray-100 active:bg-gray-200',
  };

  const sizes: Record<string, string> = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  };

  return `${base} ${variants[props.variant] || variants.primary} ${sizes[props.size] || sizes.md}`;
});
</script>
