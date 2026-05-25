<script setup lang="ts">
import { computed, ref } from 'vue';
import { useTaskStore } from '@/stores/taskStore';
import { useListStore } from '@/stores/listStore';
import { useTasks } from '@/composables/useTasks';
import { formatDate, getDueDateLabel, isOverdue } from '@/utils/dateUtils';
import Button from '@/components/common/Button.vue';
import Badge from '@/components/common/Badge.vue';

const taskStore = useTaskStore();
const listStore = useListStore();
const { isLoading } = useTasks();

const isCreateFormOpen = ref(false);

const statusLabels: Record<string, string> = {
  TO_DO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
};

const priorityLabels: Record<string, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
};

const priorityColors: Record<string, string> = {
  LOW: 'badge-info',
  MEDIUM: 'badge-warning',
  HIGH: 'badge-error',
};

const filteredTasks = computed(() => {
  const selectedListId = listStore.selectedListId;
  if (!selectedListId) return [];
  return taskStore.getTasksByListId(selectedListId);
});

const getDueDateClass = (dueDate: string | null) => {
  if (!dueDate) return '';
  if (isOverdue(dueDate)) return 'text-error font-semibold';
  const today = new Date().toISOString().split('T')[0];
  if (dueDate === today) return 'text-warning font-semibold';
  return 'text-base-600';
};

const openCreateForm = () => {
  isCreateFormOpen.value = true;
};
</script>

<template>
  <div class="flex-1 flex flex-col">
    <!-- Header -->
    <div class="p-4 border-b border-base-300 flex items-center justify-between">
      <h2 class="text-lg font-bold">Tasks</h2>
      <Button type="button" variant="primary" size="sm" @click="openCreateForm">
        + Create Task
      </Button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="isLoading" class="p-8 text-center">
        <div class="loading loading-spinner loading-lg"></div>
      </div>

      <div v-else-if="!listStore.selectedListId" class="p-8 text-center">
        <p class="text-base-600 mb-4">Select a list to view tasks</p>
      </div>

      <div v-else-if="filteredTasks.length === 0" class="p-8 text-center">
        <p class="text-base-600 mb-4">No tasks yet</p>
        <Button type="button" variant="primary" @click="openCreateForm">
          Create your first task
        </Button>
      </div>

      <!-- Desktop Table View -->
      <div v-else class="overflow-x-auto hidden md:block">
        <table class="table table-zebra w-full">
          <thead class="bg-base-300">
            <tr>
              <th>Title</th>
              <th>Status</th>
              <th>Priority</th>
              <th>Due Date</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="task in filteredTasks"
              :key="task.id"
              class="hover:bg-base-100 cursor-pointer"
            >
              <td>
                <div class="font-medium line-clamp-2">{{ task.title }}</div>
                <div v-if="task.description" class="text-sm text-base-600 line-clamp-1">
                  {{ task.description }}
                </div>
              </td>
              <td>
                <Badge type="primary">{{ statusLabels[task.status] }}</Badge>
              </td>
              <td>
                <Badge :type="priorityColors[task.priority]">
                  {{ priorityLabels[task.priority] }}
                </Badge>
              </td>
              <td>
                <span v-if="task.dueDate" :class="getDueDateClass(task.dueDate)">
                  {{ getDueDateLabel(task.dueDate) }}
                </span>
                <span v-else class="text-base-400">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Card View -->
      <div class="md:hidden p-4 space-y-3">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="card bg-base-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="card-body p-4">
            <h3 class="card-title text-base line-clamp-2">{{ task.title }}</h3>
            <p v-if="task.description" class="text-sm text-base-600 line-clamp-2">
              {{ task.description }}
            </p>
            <div class="flex flex-wrap gap-2 mt-2">
              <Badge type="primary">{{ statusLabels[task.status] }}</Badge>
              <Badge :type="priorityColors[task.priority]">
                {{ priorityLabels[task.priority] }}
              </Badge>
              <span v-if="task.dueDate" :class="['text-xs', getDueDateClass(task.dueDate)]">
                {{ getDueDateLabel(task.dueDate) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Task Modal (placeholder - will be separate component) -->
    <!-- TODO: Integrate CreateTaskForm component here -->
  </div>
</template>
