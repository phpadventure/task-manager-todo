<template>
  <div class="flex flex-col items-center justify-center py-12 px-4">
    <!-- Icon -->
    <div v-if="icon" class="mb-4 flex justify-center">
      <svg class="w-16 h-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          v-if="icon === 'inbox'"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
        />
        <path
          v-else-if="icon === 'search'"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
        />
        <path
          v-else-if="icon === 'document'"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
        />
        <path
          v-else-if="icon === 'folder'"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
        />
        <path
          v-else
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1.5"
          d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
    </div>

    <!-- Heading -->
    <h3 v-if="heading" class="mt-2 text-lg font-medium text-gray-900">
      {{ heading }}
    </h3>

    <!-- Description -->
    <p v-if="description" class="mt-1 text-sm text-gray-500 max-w-sm text-center">
      {{ description }}
    </p>

    <!-- Action Button -->
    <slot v-if="$slots.action" name="action" />
    <Button
      v-else-if="actionLabel"
      type="button"
      variant="primary"
      size="sm"
      class="mt-4"
      @click="$emit('action')"
    >
      {{ actionLabel }}
    </Button>
  </div>
</template>

<script setup lang="ts">
import Button from './Button.vue';

withDefaults(
  defineProps<{
    icon?: 'inbox' | 'search' | 'document' | 'folder' | 'info';
    heading?: string;
    description?: string;
    actionLabel?: string;
  }>(),
  {
    icon: 'inbox',
  }
);

defineEmits<{
  action: [];
}>();
</script>
