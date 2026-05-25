<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';

const { fetchCurrentUser } = useAuth();

onMounted(async () => {
  // Check if user is logged in on app start
  // If 401, fetchCurrentUser silently handles it (user stays null)
  // Router guard will then redirect to /auth if needed
  await fetchCurrentUser();
});
</script>

<template>
  <RouterView />
</template>
