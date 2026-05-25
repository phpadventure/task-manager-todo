<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';
import router from '@/router';

const { fetchCurrentUser, isAuthenticated } = useAuth();

onMounted(async () => {
  await fetchCurrentUser();
});
</script>

<template>
  <RouterView />
</template>
