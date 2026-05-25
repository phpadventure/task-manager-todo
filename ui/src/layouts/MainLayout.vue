<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside class="w-64 bg-white border-r border-gray-200 overflow-y-auto hidden md:block">
      <slot name="sidebar" />
    </aside>

    <!-- Mobile sidebar toggle -->
    <div class="md:hidden fixed top-4 left-4 z-50">
      <button
        class="p-2 bg-white rounded-lg border border-gray-200 hover:bg-gray-50"
        @click="sidebarOpen = !sidebarOpen"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 6h16M4 12h16M4 18h16"
          />
        </svg>
      </button>
    </div>

    <!-- Mobile sidebar overlay -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 bg-black/50 md:hidden z-40"
      @click="sidebarOpen = false"
    />

    <!-- Main content -->
    <main class="flex-1 overflow-y-auto">
      <div class="md:pt-0 pt-16">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const sidebarOpen = ref(false);
</script>
