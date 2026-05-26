<script setup lang="ts">
import { computed, ref } from 'vue';
import { useAuth } from '@/composables/useAuth';
import NotificationSettings from '@/components/settings/NotificationSettings.vue';
import HelpModal from '@/components/help/HelpModal.vue';

const { user, logout, isLoading } = useAuth();
const isMenuOpen = ref(false);
const isSettingsOpen = ref(false);
const isHelpOpen = ref(false);

const userInitials = computed(() => {
  if (!user.value?.displayName) return '?';
  return user.value.displayName
    .split(' ')
    .slice(0, 2)
    .map((word) => word[0].toUpperCase())
    .join('');
});

const handleLogout = async () => {
  await logout();
  isMenuOpen.value = false;
};

const openSettings = () => {
  isSettingsOpen.value = true;
  isMenuOpen.value = false;
};

const openHelp = () => {
  isHelpOpen.value = true;
  isMenuOpen.value = false;
};

// Close menu when clicking outside
const closeMenu = () => {
  isMenuOpen.value = false;
};
</script>

<template>
  <div class="relative">
    <!-- Avatar Button -->
    <button
      type="button"
      class="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
      :aria-label="`User menu for ${user?.displayName}`"
      @click="isMenuOpen = !isMenuOpen"
    >
      <!-- Avatar -->
      <img
        v-if="user?.avatarUrl"
        :src="user.avatarUrl"
        :alt="user.displayName"
        class="w-8 h-8 rounded-full"
      />
      <div
        v-else
        class="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-medium"
      >
        {{ userInitials }}
      </div>

      <!-- Name -->
      <span class="hidden sm:block text-sm font-medium text-gray-900">{{ user?.displayName }}</span>

      <!-- Chevron -->
      <svg
        class="w-4 h-4 text-gray-600 transition-transform"
        :class="{ 'rotate-180': isMenuOpen }"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M19 14l-7 7m0 0l-7-7m7 7V3"
        />
      </svg>
    </button>

    <!-- Dropdown Menu -->
    <Transition name="menu-fade">
      <div
        v-if="isMenuOpen"
        class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-10"
        role="menu"
        aria-orientation="vertical"
      >
        <!-- User Info -->
        <div class="px-4 py-3 border-b border-gray-100">
          <p class="text-sm font-medium text-gray-900">{{ user?.displayName }}</p>
          <p class="text-xs text-gray-500 truncate">{{ user?.email }}</p>
        </div>

        <!-- Menu Items -->
        <div class="py-1">
          <button
            type="button"
            class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
            role="menuitem"
            @click="openSettings"
          >
            <svg class="inline w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
            </svg>
            Settings
          </button>
          <button
            type="button"
            class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
            role="menuitem"
            @click="openHelp"
          >
            <svg class="inline w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            Help
          </button>
        </div>

        <!-- Logout Button -->
        <div class="border-t border-gray-100 py-1">
          <button
            type="button"
            class="w-full text-left px-4 py-2 text-sm text-red-700 hover:bg-red-50 transition-colors disabled:opacity-50"
            role="menuitem"
            :disabled="isLoading"
            @click="handleLogout"
          >
            <svg class="inline w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
            {{ isLoading ? 'Logging out...' : 'Logout' }}
          </button>
        </div>
      </div>
    </Transition>

    <!-- Backdrop -->
    <Teleport to="body">
      <Transition name="backdrop-fade">
        <div v-if="isMenuOpen" class="fixed inset-0 z-9" @click="closeMenu" />
      </Transition>
    </Teleport>
  </div>

  <!-- Notification Settings Modal -->
  <NotificationSettings v-model="isSettingsOpen" />

  <!-- Help Modal -->
  <HelpModal v-model="isHelpOpen" />
</template>

<style scoped>
.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: opacity 0.15s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
}

.backdrop-fade-enter-active,
.backdrop-fade-leave-active {
  transition: opacity 0.15s ease;
}

.backdrop-fade-enter-from,
.backdrop-fade-leave-to {
  opacity: 0;
}
</style>
