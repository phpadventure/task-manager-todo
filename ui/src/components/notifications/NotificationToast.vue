<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import Badge from '@/components/common/Badge.vue';
import Button from '@/components/common/Button.vue';
import { getDueDateLabel } from '@/utils/dateUtils';

interface Notification {
  id: string;
  taskId: string;
  title: string;
  reason: 'overdue' | 'due-soon';
  dueDate: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  sentAt: string;
}

interface Props {
  notification: Notification;
}

withDefaults(defineProps<Props>(), {});

const emit = defineEmits<{
  acknowledge: [];
}>();

const isVisible = ref(true);
let dismissTimer: NodeJS.Timeout;

onMounted(() => {
  dismissTimer = setTimeout(() => {
    isVisible.value = false;
  }, 5000);
});

onUnmounted(() => {
  if (dismissTimer) {
    clearTimeout(dismissTimer);
  }
});

const handleAcknowledge = () => {
  emit('acknowledge');
  isVisible.value = false;
};

const handleClose = () => {
  isVisible.value = false;
};

const reasonLabel = (reason: string) => {
  const labels: Record<string, string> = {
    overdue: 'Overdue',
    'due-soon': 'Due Soon',
  };
  return labels[reason] || reason;
};

const reasonBadgeType = (reason: string) => {
  return reason === 'overdue' ? 'error' : 'warning';
};

const priorityBadgeType = (priority: string) => {
  const types: Record<string, string> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'error',
  };
  return types[priority] || 'primary';
};

const priorityLabel = (priority: string) => {
  const labels: Record<string, string> = {
    LOW: 'Low',
    MEDIUM: 'Medium',
    HIGH: 'High',
  };
  return labels[priority] || priority;
};
</script>

<template>
  <div
    v-if="isVisible"
    class="bg-white rounded-lg shadow-lg border border-base-300 p-4 max-w-sm w-full animate-slide-in"
  >
    <!-- Header with close -->
    <div class="flex items-start justify-between mb-2">
      <div class="flex-1">
        <h3 class="font-bold text-base line-clamp-2">{{ notification.title }}</h3>
      </div>
      <button class="ml-2 text-base-400 hover:text-base-600 flex-shrink-0" @click="handleClose">
        ✕
      </button>
    </div>

    <!-- Task Info -->
    <div class="flex flex-wrap gap-2 mb-3">
      <Badge :type="reasonBadgeType(notification.reason)">
        {{ reasonLabel(notification.reason) }}
      </Badge>
      <Badge :type="priorityBadgeType(notification.priority)">
        {{ priorityLabel(notification.priority) }}
      </Badge>
    </div>

    <!-- Due Date -->
    <p class="text-sm text-base-600 mb-3">
      Due: <span class="font-medium">{{ getDueDateLabel(notification.dueDate) }}</span>
    </p>

    <!-- Actions -->
    <div class="flex gap-2">
      <Button type="button" variant="primary" size="sm" class="flex-1" @click="handleAcknowledge">
        Got it
      </Button>
      <Button type="button" variant="ghost" size="sm" @click="handleClose"> Dismiss </Button>
    </div>
  </div>
</template>

<style scoped>
@keyframes slide-in {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-slide-in {
  animation: slide-in 0.3s ease-out;
}
</style>
