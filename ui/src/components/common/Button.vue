<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[
      'btn',
      `btn-${variant}`,
      `btn-${size}`,
      { 'opacity-50 cursor-not-allowed': disabled || loading },
    ]"
    v-bind="$attrs"
  >
    <span
      v-if="loading"
      class="inline-block w-4 h-4 mr-2 border-2 border-current border-t-transparent rounded-full animate-spin"
    />
    <slot />
  </button>
</template>

<script setup lang="ts">
withDefaults(
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
</script>
