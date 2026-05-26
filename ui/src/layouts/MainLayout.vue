<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Desktop Sidebar -->
    <aside
      class="hidden lg:flex lg:w-80 bg-white border-r border-gray-200 flex-col overflow-y-auto shadow-sm"
    >
      <slot name="sidebar" />
    </aside>

    <!-- Mobile Header -->
    <div
      class="lg:hidden fixed top-0 left-0 right-0 h-12 md:h-14 bg-white border-b border-gray-200 flex items-center px-3 md:px-4 z-40 shadow-sm"
    >
      <button
        class="p-2 -ml-2 hover:bg-gray-100 rounded-lg transition-colors"
        aria-label="Toggle sidebar"
        @click="sidebarOpen = !sidebarOpen"
      >
        <svg
          class="w-5 h-5 md:w-6 md:h-6 text-gray-600 inline-block"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 6h16M4 12h16M4 18h16"
          />
        </svg>
      </button>
      <h1 class="ml-4 font-bold text-sm md:text-lg text-gray-900">Tasks</h1>
    </div>

    <!-- Mobile Sidebar Overlay -->
    <transition name="slide-overlay">
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 bg-black/40 lg:hidden z-35"
        @click="sidebarOpen = false"
      />
    </transition>

    <!-- Mobile Sidebar Drawer -->
    <transition name="slide-drawer">
      <aside
        v-if="sidebarOpen"
        class="fixed left-0 top-12 md:top-14 bottom-0 w-72 bg-white z-40 overflow-y-auto lg:hidden shadow-2xl"
      >
        <div
          class="p-4 border-b border-gray-200 flex items-center justify-between sticky top-0 bg-white z-10"
        >
          <h2 class="font-bold text-gray-900">My Lists</h2>
          <button
            class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            aria-label="Close sidebar"
            @click="sidebarOpen = false"
          >
            <svg class="w-6 h-6 inline-block" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
        <slot name="sidebar" />
      </aside>
    </transition>

    <!-- Main Content -->
    <main class="flex-1 overflow-hidden flex flex-col bg-gray-50">
      <div class="flex-1 overflow-y-auto pt-12 md:pt-14 lg:pt-0">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
const sidebarOpen = ref(false);
</script>

<style scoped>
.slide-overlay-enter-active,
.slide-overlay-leave-active {
  transition: opacity 0.2s ease;
}
.slide-overlay-enter-from,
.slide-overlay-leave-to {
  opacity: 0;
}

.slide-drawer-enter-active,
.slide-drawer-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-drawer-enter-from {
  transform: translateX(-100%);
}
.slide-drawer-leave-to {
  transform: translateX(-100%);
}
</style>
