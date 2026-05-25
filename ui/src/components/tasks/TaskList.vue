<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
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
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024);

const handleResize = () => {
  windowWidth.value = window.innerWidth;
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

const isDesktop = computed(() => windowWidth.value >= 1024);

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
            class="animate-spin rounded-full h-8 w-8 border-2 border-gray-300 border-t-blue-600 mx-auto mb-2"
          ></div>
          <p class="text-sm text-gray-500">Loading tasks...</p>
        </div>
      </div>

      <div v-else-if="!listStore.selectedListId" class="flex items-center justify-center h-64">
        <div class="text-center">
          <svg
            class="mx-auto h-12 w-12 text-gray-400"
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
          <p class="text-gray-600 mt-2">Select a list to view tasks</p>
        </div>
      </div>

      <div v-else-if="filteredTasks.length === 0" class="flex items-center justify-center h-64">
        <div class="text-center">
          <svg
            class="mx-auto h-12 w-12 text-gray-400"
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
          <p class="text-gray-600 mt-2 mb-4">No tasks yet</p>
          <Button type="button" variant="primary" @click="openCreateForm">
            Create your first task
          </Button>
        </div>
      </div>

      <!-- Desktop Task Cards (lg and up) -->
      <div v-else-if="isDesktop" class="p-6">
        <div class="space-y-3">
          <div
            v-for="task in filteredTasks"
            :key="task.id"
            class="group relative bg-gradient-to-br from-white to-gray-50 rounded-xl border border-gray-200 shadow-md hover:shadow-xl hover:border-blue-400 transition-all duration-300 cursor-pointer overflow-hidden"
          >
            <div
              class="absolute inset-0 bg-gradient-to-r from-blue-50/0 to-blue-50/0 group-hover:from-blue-50/50 group-hover:to-transparent transition-all duration-300 pointer-events-none"
            />

            <div class="relative flex items-center p-5 gap-4">
              <!-- Status Indicator -->
              <div
                class="flex-shrink-0 w-1.5 h-14 rounded-full"
                :class="{
                  'bg-gradient-to-b from-blue-500 to-blue-600 shadow-lg':
                    task.status === 'IN_PROGRESS',
                  'bg-gradient-to-b from-amber-500 to-amber-600 shadow-lg': task.status === 'TO_DO',
                  'bg-gradient-to-b from-emerald-500 to-emerald-600 shadow-lg':
                    task.status === 'DONE',
                }"
              />

              <!-- Title & Description -->
              <div class="flex-1 min-w-0">
                <h3
                  class="font-bold text-gray-900 text-base line-clamp-1 group-hover:text-blue-700 transition-colors"
                >
                  {{ task.title }}
                </h3>
                <p v-if="task.description" class="text-sm text-gray-500 line-clamp-1 mt-0.5">
                  {{ task.description }}
                </p>
              </div>

              <!-- Status & Priority Badges -->
              <div class="flex-shrink-0 flex items-center gap-2">
                <Badge type="primary">{{ statusLabels[task.status] }}</Badge>
                <Badge :type="priorityColors[task.priority]">
                  {{ priorityLabels[task.priority] }}
                </Badge>
              </div>

              <!-- Due Date -->
              <div class="flex-shrink-0 text-right min-w-max">
                <span
                  v-if="task.dueDate"
                  :class="['text-sm font-semibold', getDueDateClass(task.dueDate)]"
                >
                  {{ getDueDateLabel(task.dueDate) }}
                </span>
                <span v-else class="text-sm text-gray-300">—</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Mobile Card View (below lg) -->
      <div v-else class="p-4 md:p-6 space-y-2.5">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="group bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer p-4 hover:border-blue-300 overflow-hidden relative"
        >
          <!-- Status Indicator Top Border -->
          <div
            class="absolute top-0 left-0 right-0 h-1.5 rounded-t-lg"
            :class="{
              'bg-blue-500': task.status === 'IN_PROGRESS',
              'bg-amber-500': task.status === 'TO_DO',
              'bg-emerald-500': task.status === 'DONE',
            }"
          />

          <!-- Title & Description -->
          <div class="mb-3 pt-2">
            <h3
              class="font-semibold text-gray-900 text-sm line-clamp-2 group-hover:text-blue-600 transition-colors"
            >
              {{ task.title }}
            </h3>
            <p v-if="task.description" class="text-xs text-gray-500 line-clamp-2 mt-1">
              {{ task.description }}
            </p>
          </div>

          <!-- Badges Row -->
          <div class="flex flex-wrap gap-2 items-center">
            <Badge type="primary">{{ statusLabels[task.status] }}</Badge>
            <Badge :type="priorityColors[task.priority]">
              {{ priorityLabels[task.priority] }}
            </Badge>
            <span
              v-if="task.dueDate"
              :class="[
                'text-xs font-medium px-2.5 py-1 rounded-full bg-gray-100',
                getDueDateClass(task.dueDate),
              ]"
            >
              {{ getDueDateLabel(task.dueDate) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Task Modal (placeholder - will be separate component) -->
    <!-- TODO: Integrate CreateTaskForm component here -->
  </div>
</template>
