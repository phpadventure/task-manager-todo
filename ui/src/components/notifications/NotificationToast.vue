<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import Badge from '@/components/common/Badge.vue';
import Button from '@/components/common/Button.vue';
import { getDueDateLabel } from '@/utils/dateUtils';
import { useSettingsStore } from '@/stores/settingsStore';

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

const props = withDefaults(defineProps<Props>(), {});

const emit = defineEmits<{
  acknowledge: [];
  dismiss: [];
}>();

const settingsStore = useSettingsStore();
const isVisible = ref(true);
let dismissTimer: NodeJS.Timeout;

onMounted(() => {
  // eslint-disable-next-line no-undef
  console.log('[Toast] Rendering notification:', {
    title: props.notification.title,
    dueDate: props.notification.dueDate,
    reason: props.notification.reason,
    id: props.notification.id,
  });
  dismissTimer = setTimeout(() => {
    isVisible.value = false;
  }, settingsStore.toastDismissTime);
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
  emit('dismiss');
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
    class="relative bg-white rounded-lg shadow-lg border-l-4 p-4 max-w-sm w-full animate-slide-in"
    :class="{
      'border-l-red-500': notification.reason === 'overdue',
      'border-l-amber-500': notification.reason === 'due-soon',
    }"
  >
    <!-- Task Title (Prominent - FIRST) -->
    <h3 class="font-bold text-base text-gray-900 mb-3 pr-7 break-words">
      {{ notification.title || 'Untitled Task' }}
    </h3>

    <!-- Close button -->
    <button
      class="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-xl leading-none"
      @click="handleClose"
    >
      ✕
    </button>

    <!-- Reason & Priority Badges -->
    <div class="flex flex-wrap gap-2 mb-3">
      <Badge :type="reasonBadgeType(notification.reason)">
        {{ reasonLabel(notification.reason) }}
      </Badge>
      <Badge :type="priorityBadgeType(notification.priority)">
        {{ priorityLabel(notification.priority) }}
      </Badge>
    </div>

    <!-- Due Date Info -->
    <div class="mb-4 p-2 bg-gray-50 rounded text-sm">
      <p class="text-gray-700">
        <span class="font-medium">Due:</span>
        <span class="text-gray-900 font-semibold">{{ getDueDateLabel(notification.dueDate) }}</span>
      </p>
    </div>

    <!-- Actions -->
    <div class="flex gap-2">
      <Button type="button" variant="primary" size="sm" class="flex-1" @click="handleAcknowledge">
        Got it
      </Button>
      <Button type="button" variant="ghost" size="sm" class="flex-1" @click="handleClose">
        Dismiss
      </Button>
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
