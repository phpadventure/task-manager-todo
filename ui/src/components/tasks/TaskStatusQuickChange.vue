<script setup lang="ts">
import { computed } from 'vue';
import { useTasks } from '@/composables/useTasks';

interface Props {
  taskId: string;
  currentStatus: 'TO_DO' | 'IN_PROGRESS' | 'DONE';
}

const props = defineProps<Props>();

const emit = defineEmits<{
  statusChanged: [status: 'TO_DO' | 'IN_PROGRESS' | 'DONE'];
}>();

const { updateTaskStatus, isUpdating } = useTasks();

const statusOptions = [
  { value: 'TO_DO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'DONE', label: 'Done' },
];

const statusColors: Record<string, string> = {
  TO_DO: 'bg-gray-100 text-gray-800 hover:bg-gray-200',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200',
  DONE: 'bg-green-100 text-green-800 hover:bg-green-200',
};

const currentStatusColor = computed(() => statusColors[props.currentStatus] || statusColors.TO_DO);

const handleStatusChange = async (newStatus: 'TO_DO' | 'IN_PROGRESS' | 'DONE') => {
  if (newStatus === props.currentStatus) return;

  await updateTaskStatus(props.taskId, newStatus);
  emit('statusChanged', newStatus);
};
</script>

<template>
  <div class="relative inline-block">
    <div class="flex items-center gap-1">
      <select
        :value="currentStatus"
        :disabled="isUpdating"
        class="appearance-none px-3 py-1.5 text-sm font-medium rounded-lg border-0 cursor-pointer transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :class="currentStatusColor"
        @change="
          handleStatusChange(
            ($event.target as HTMLSelectElement).value as 'TO_DO' | 'IN_PROGRESS' | 'DONE'
          )
        "
      >
        <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
      <div
        v-if="isUpdating"
        class="inline-block w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"
      />
    </div>
  </div>
</template>
