<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import Badge from '@/components/common/Badge.vue';
import Button from '@/components/common/Button.vue';
import Toast from '@/components/common/Toast.vue';
import { getDueDateLabel } from '@/utils/dateUtils';
import { useSettingsStore } from '@/stores/settingsStore';

interface TaskNotification {
  id: string;
  type: 'task';
  taskId: string;
  title: string;
  reason: 'overdue' | 'due-soon';
  dueDate: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  sentAt: string;
}

interface ErrorNotification {
  id: string;
  type: 'error';
  message: string;
  duration?: number;
}

type Notification = TaskNotification | ErrorNotification;

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

const isErrorNotification = computed(() => props.notification.type === 'error');
const isTaskNotification = computed(() => props.notification.type === 'task');

onMounted(() => {
  if (isTaskNotification.value) {
    const taskNotif = props.notification as TaskNotification;
    // eslint-disable-next-line no-undef
    console.log('[Toast] Rendering notification:', {
      title: taskNotif.title,
      dueDate: taskNotif.dueDate,
      reason: taskNotif.reason,
      id: taskNotif.id,
    });
    dismissTimer = setTimeout(() => {
      emit('dismiss');
    }, settingsStore.toastDismissTime);
  } else if (isErrorNotification.value) {
    const errorNotif = props.notification as ErrorNotification;
    dismissTimer = setTimeout(() => {
      emit('dismiss');
    }, errorNotif.duration || 5000);
  }
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
  <!-- Error Toast (using Toast component) -->
  <Toast
    v-if="isErrorNotification && isVisible"
    type="error"
    :message="(notification as ErrorNotification).message"
    @close="handleClose"
  />

  <!-- Task Notification Toast -->
  <div
    v-if="isTaskNotification && isVisible"
    class="relative bg-white rounded-lg shadow-lg border-l-4 p-4 max-w-sm w-full animate-slide-in"
    :class="{
      'border-l-red-500': (notification as TaskNotification).reason === 'overdue',
      'border-l-amber-500': (notification as TaskNotification).reason === 'due-soon',
    }"
  >
    <!-- Task Title (Prominent - FIRST) -->
    <h3 class="font-bold text-base text-gray-900 mb-3 pr-7 break-words">
      {{ (notification as TaskNotification).title || 'Untitled Task' }}
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
      <Badge :type="reasonBadgeType((notification as TaskNotification).reason)">
        {{ reasonLabel((notification as TaskNotification).reason) }}
      </Badge>
      <Badge :type="priorityBadgeType((notification as TaskNotification).priority)">
        {{ priorityLabel((notification as TaskNotification).priority) }}
      </Badge>
    </div>

    <!-- Due Date Info -->
    <div class="mb-4 p-2 bg-gray-50 rounded text-sm">
      <p class="text-gray-700">
        <span class="font-medium">Due:</span>
        <span class="text-gray-900 font-semibold">{{
          getDueDateLabel((notification as TaskNotification).dueDate)
        }}</span>
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
