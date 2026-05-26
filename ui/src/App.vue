<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useNotificationStore } from '@/stores/notificationStore';
import { useAuth } from '@/composables/useAuth';
import { useNotifications } from '@/composables/useNotifications';
import NotificationToast from '@/components/notifications/NotificationToast.vue';

const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const { fetchCurrentUser } = useAuth();
const { connect, disconnect, acknowledge } = useNotifications();

onMounted(async () => {
  // Check if user is logged in on app start
  await fetchCurrentUser();

  // If authenticated, connect to WebSocket for notifications
  if (authStore.isAuthenticated) {
    await connect();
  }
});

onUnmounted(() => {
  disconnect();
});
</script>

<template>
  <div id="app">
    <!-- Router view -->
    <RouterView />

    <!-- Global Notification Container (fixed position, top-right) -->
    <div class="fixed top-4 right-4 z-50 space-y-2 max-w-md">
      <NotificationToast
        v-for="notif in notificationStore.notifications"
        :key="notif.id"
        :notification="notif"
        @acknowledge="acknowledge(notif.id)"
        @dismiss="notificationStore.removeNotification(notif.id)"
      />
    </div>
  </div>
</template>
