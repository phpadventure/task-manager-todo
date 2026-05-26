<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black bg-opacity-50 cursor-pointer" @click="closeModal" />

        <!-- Modal -->
        <div
          class="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 z-10 focus:outline-none max-h-96 flex flex-col overflow-hidden"
          role="dialog"
          aria-modal="true"
          @keydown.esc="closeModal"
        >
          <!-- Header -->
          <div
            v-if="$slots.header"
            class="flex items-center justify-between p-6 border-b border-gray-200"
          >
            <slot name="header" />
            <button
              type="button"
              class="text-gray-500 hover:text-gray-700 transition-colors"
              aria-label="Close modal"
              @click="isOpen = false"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="p-6 overflow-y-auto flex-1">
            <slot />
          </div>

          <!-- Footer -->
          <div
            v-if="$slots.footer"
            class="flex items-center justify-end gap-3 p-6 border-t border-gray-200"
          >
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { Teleport, Transition } from 'vue';
import { computed, watch } from 'vue';

const props = defineProps<{
  modelValue: boolean;
  closeOnBackdropClick?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
});

const closeModal = () => {
  if (props.closeOnBackdropClick !== false) {
    isOpen.value = false;
  }
};

// Prevent body scroll when modal is open
watch(
  () => isOpen.value,
  (open) => {
    if (open) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }
);
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
