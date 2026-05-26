<script setup lang="ts">
import { computed, ref } from 'vue';
import { useTaskStore } from '@/stores/taskStore';
import { useListStore } from '@/stores/listStore';
import { useTasks } from '@/composables/useTasks';
import { getDueDateLabel, isOverdue } from '@/utils/dateUtils';
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
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'danger',
};

const filteredTasks = computed(() => {
  const selectedListId = listStore.selectedListId;
  if (!selectedListId) return [];
  return taskStore.getTasksByListId(selectedListId);
});

const getDueDateClass = (dueDate: string | null) => {
  if (!dueDate) return '';
  if (isOverdue(dueDate)) return 'text-red-600 font-semibold';
  const today = new Date().toISOString().split('T')[0];
  if (dueDate === today) return 'text-amber-600 font-semibold';
  return 'text-gray-600';
};

const openCreateForm = () => {
  isCreateFormOpen.value = true;
};
</script>

<template>
  <div class="flex-1 flex flex-col">
    <!-- Header -->
    <div
      class="px-4 md:px-6 py-4 border-b border-gray-200 bg-white flex items-center justify-between sticky top-0 z-10"
    >
      <h2 class="text-lg md:text-xl font-bold text-gray-900">Tasks</h2>
      <Button
        type="button"
        variant="primary"
        size="sm"
        class="whitespace-nowrap"
        @click="openCreateForm"
      >
        + Create Task
      </Button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto bg-gray-50">
      <div v-if="isLoading" class="flex items-center justify-center h-40">
        <div class="text-center">
          <div
            class="animate-spin rounded-full h-6 w-6 md:h-8 md:w-8 border-2 border-gray-300 border-t-blue-600 mx-auto mb-2"
          ></div>
          <p class="text-xs md:text-sm text-gray-500">Loading tasks...</p>
        </div>
      </div>

      <div v-else-if="!listStore.selectedListId" class="flex items-center justify-center h-64">
        <div class="text-center px-4">
          <svg
            class="mx-auto h-8 w-8 md:h-12 md:w-12 text-gray-400 inline-block"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <p class="text-gray-600 mt-2 text-sm md:text-base">Select a list to view tasks</p>
        </div>
      </div>

      <div v-else-if="filteredTasks.length === 0" class="flex items-center justify-center h-64">
        <div class="text-center px-4">
          <svg
            class="mx-auto h-8 w-8 md:h-12 md:w-12 text-gray-400 inline-block"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
            />
          </svg>
          <p class="text-gray-600 mt-2 mb-4 text-sm md:text-base">No tasks yet</p>
          <Button type="button" variant="primary" @click="openCreateForm">
            Create your first task
          </Button>
        </div>
      </div>

      <!-- Task Cards - Responsive Layout -->
      <div class="p-3 md:p-4 lg:p-6">
        <div class="space-y-2 md:space-y-3 lg:space-y-4">
          <div
            v-for="task in filteredTasks"
            :key="task.id"
            class="group relative rounded-lg overflow-hidden transition-all duration-300 cursor-pointer"
            :style="{
              background: 'linear-gradient(135deg, #ffffff 0%, #f9fafb 100%)',
              border: '1px solid #e5e7eb',
              boxShadow: '0 1px 3px rgba(0, 0, 0, 0.08)',
            }"
            @mouseenter="
              (e) => {
                (e.currentTarget as HTMLElement).style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.12)';
                (e.currentTarget as HTMLElement).style.borderColor = '#3b82f6';
              }
            "
            @mouseleave="
              (e) => {
                (e.currentTarget as HTMLElement).style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.08)';
                (e.currentTarget as HTMLElement).style.borderColor = '#e5e7eb';
              }
            "
          >
            <!-- Status Indicator Border (Left edge) -->
            <div
              class="absolute top-0 left-0 bottom-0 w-1.5"
              :style="{
                background:
                  task.status === 'IN_PROGRESS'
                    ? '#3b82f6'
                    : task.status === 'TO_DO'
                      ? '#f59e0b'
                      : '#10b981',
              }"
            />

            <!-- Responsive Layout: Mobile (column) -> Desktop (row) -->
            <div
              class="flex flex-col lg:flex-row lg:items-start p-3 md:p-4 pl-3 md:pl-4 gap-2 md:gap-3"
            >
              <!-- Title & Description -->
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-900 text-sm md:text-base line-clamp-1">
                  {{ task.title }}
                </h3>
                <p
                  v-if="task.description"
                  class="text-xs md:text-sm text-gray-600 line-clamp-1 mt-1"
                >
                  {{ task.description }}
                </p>
              </div>

              <!-- Badges & Due Date Row -->
              <div class="flex flex-wrap gap-1.5 md:gap-2 lg:flex-nowrap items-start">
                <Badge type="primary">{{ statusLabels[task.status] }}</Badge>
                <Badge :type="priorityColors[task.priority]">
                  {{ priorityLabels[task.priority] }}
                </Badge>
                <span
                  v-if="task.dueDate"
                  :class="[
                    'text-xs font-medium px-2 py-0.5 rounded-full',
                    getDueDateClass(task.dueDate),
                  ]"
                  :style="{ backgroundColor: '#f0fdf4' }"
                >
                  {{ getDueDateLabel(task.dueDate) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Task Modal (placeholder - will be separate component) -->
    <!-- TODO: Integrate CreateTaskForm component here -->
  </div>
</template>
