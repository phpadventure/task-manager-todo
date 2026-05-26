<template>
  <Transition name="toast-slide" @enter="startAutoClose" @leave="clearAutoClose">
    <div
      v-if="isVisible"
      :class="toastClasses"
      role="alert"
      @mouseenter="pauseAutoClose"
      @mouseleave="resumeAutoClose"
    >
      <!-- Icon -->
      <div :class="iconClasses">
        <component :is="icon" class="w-5 h-5" />
      </div>

      <!-- Content -->
      <div class="flex-1">
        <p v-if="title" class="font-medium text-sm">{{ title }}</p>
        <p v-if="message" class="text-sm mt-1">{{ message }}</p>
      </div>

      <!-- Close Button -->
      <button
        type="button"
        class="text-gray-400 hover:text-gray-600 ml-2 flex-shrink-0"
        @click="close"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { Transition } from 'vue';

interface ToastIcon {
  success: string;
  error: string;
  info: string;
  warning: string;
}

const props = withDefaults(
  defineProps<{
    type?: 'success' | 'error' | 'info' | 'warning';
    title?: string;
    message?: string;
    duration?: number;
  }>(),
  {
    type: 'info',
    duration: 5000,
  }
);

const emit = defineEmits<{
  close: [];
}>();

const isVisible = ref(true);
let autoCloseTimer: ReturnType<typeof setTimeout> | null = null;
let pausedTime: number | null = null;
let pausedAt: number | null = null;

const toastClasses = computed(() => {
  const base = 'flex items-start gap-3 p-4 rounded-lg shadow-lg border';
  const types: Record<string, string> = {
    success: 'bg-green-50 border-green-200 text-green-800',
    error: 'bg-red-50 border-red-200 text-red-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800',
    warning: 'bg-amber-50 border-amber-200 text-amber-800',
  };
  return `${base} ${types[props.type]}`;
});

const iconClasses = computed(() => {
  const base = 'flex-shrink-0 mt-0.5';
  const types: Record<string, string> = {
    success: 'text-green-600',
    error: 'text-red-600',
    info: 'text-blue-600',
    warning: 'text-amber-600',
  };
  return `${base} ${types[props.type]}`;
});

const iconComponents: ToastIcon = {
  success: 'CheckCircleIcon',
  error: 'ExclamationIcon',
  info: 'InformationCircleIcon',
  warning: 'ExclamationIcon',
};

const icon = computed(() => iconComponents[props.type]);

const close = () => {
  isVisible.value = false;
  clearAutoClose();
  emit('close');
};

const startAutoClose = () => {
  autoCloseTimer = setTimeout(() => {
    close();
  }, props.duration);
};

const pauseAutoClose = () => {
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer);
    pausedAt = Date.now();
    pausedTime = props.duration;
  }
};

const resumeAutoClose = () => {
  if (pausedAt && pausedTime) {
    const elapsed = Date.now() - pausedAt;
    const remaining = pausedTime - elapsed;
    pausedAt = null;
    pausedTime = null;

    autoCloseTimer = setTimeout(
      () => {
        close();
      },
      Math.max(0, remaining)
    );
  }
};

const clearAutoClose = () => {
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer);
    autoCloseTimer = null;
  }
};

onBeforeUnmount(() => {
  clearAutoClose();
});
</script>

<style scoped>
.toast-slide-enter-active {
  animation: slideIn 0.3s ease-out;
}

.toast-slide-leave-active {
  animation: slideOut 0.2s ease-in;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}
</style>
