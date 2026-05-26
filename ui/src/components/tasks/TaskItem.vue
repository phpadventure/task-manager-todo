<script setup lang="ts">
import { computed } from 'vue';
import Badge from '@/components/common/Badge.vue';
import { formatDate, getDueDateLabel } from '@/utils/dateUtils';

interface Task {
  id: string;
  title: string;
  description?: string;
  status: 'TO_DO' | 'IN_PROGRESS' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  dueDate?: string;
}

interface Props {
  task: Task;
  showDescription?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showDescription: true,
});

const emit = defineEmits<{
  edit: [];
  delete: [];
}>();

const statusBadgeType = computed(() => {
  const types: Record<string, string> = {
    TO_DO: 'primary',
    IN_PROGRESS: 'warning',
    DONE: 'success',
  };
  return types[props.task.status] || 'primary';
});

const priorityBadgeType = computed(() => {
  const types: Record<string, string> = {
    LOW: 'badge-info',
    MEDIUM: 'warning',
    HIGH: 'danger',
  };
  return types[props.task.priority] || 'primary';
});

const statusLabel = computed(() => {
  const labels: Record<string, string> = {
    TO_DO: 'To Do',
    IN_PROGRESS: 'In Progress',
    DONE: 'Done',
  };
  return labels[props.task.status] || props.task.status;
});

const priorityLabel = computed(() => {
  const labels: Record<string, string> = {
    LOW: 'Low',
    MEDIUM: 'Medium',
    HIGH: 'High',
  };
  return labels[props.task.priority] || props.task.priority;
});

const dueDateDisplay = computed(() => {
  if (!props.task.dueDate) return null;
  return {
    label: getDueDateLabel(props.task.dueDate),
    formatted: formatDate(props.task.dueDate),
  };
});
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
    <!-- Header: Title + Actions -->
    <div class="flex items-start justify-between gap-2 mb-2">
      <div class="flex-1 min-w-0">
        <h3 class="font-medium text-gray-900 truncate">{{ task.title }}</h3>
      </div>
      <div class="flex gap-1 flex-shrink-0">
        <button
          type="button"
          class="text-gray-400 hover:text-blue-600 transition-colors p-1"
          aria-label="Edit task"
          @click="$emit('edit')"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
            />
          </svg>
        </button>
        <button
          type="button"
          class="text-gray-400 hover:text-red-600 transition-colors p-1"
          aria-label="Delete task"
          @click="$emit('delete')"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- Description -->
    <p v-if="showDescription && description" class="text-sm text-gray-600 mb-3 line-clamp-2">
      {{ description }}
    </p>

    <!-- Badges and Due Date -->
    <div class="flex flex-wrap gap-2 items-center">
      <Badge :type="statusBadgeType">{{ statusLabel }}</Badge>
      <Badge :type="priorityBadgeType">{{ priorityLabel }}</Badge>
      <div v-if="dueDateDisplay" class="flex items-center gap-1 text-xs text-gray-600">
        <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path
            fill-rule="evenodd"
            d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v2h16V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h12a1 1 0 100-2H6z"
            clip-rule="evenodd"
          />
        </svg>
        <span>{{ dueDateDisplay.formatted }}</span>
      </div>
    </div>
  </div>
</template>
