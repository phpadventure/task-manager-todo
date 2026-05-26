<script setup lang="ts">
import { computed } from 'vue';
import { useTaskStore } from '@/stores/taskStore';
import { useListStore } from '@/stores/listStore';
import { useTasks } from '@/composables/useTasks';
import Button from '@/components/common/Button.vue';
import TaskItem from '@/components/tasks/TaskItem.vue';

const taskStore = useTaskStore();
const listStore = useListStore();
const { isLoading } = useTasks();

const emit = defineEmits<{
  'edit-task': [taskId: string];
  'delete-task': [taskId: string];
  'create-task': [];
}>();

const filteredTasks = computed(() => {
  const selectedListId = listStore.selectedListId;
  if (!selectedListId) return [];
  return taskStore.getTasksByListId(selectedListId);
});

const openCreateForm = () => {
  emit('create-task');
};

const handleEditTask = (taskId: string) => {
  emit('edit-task', taskId);
};

const handleDeleteTask = (taskId: string) => {
  emit('delete-task', taskId);
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

      <!-- Task Items - Using TaskItem component -->
      <div class="p-3 md:p-4 lg:p-6">
        <div class="space-y-2 md:space-y-3 lg:space-y-4">
          <TaskItem
            v-for="task in filteredTasks"
            :key="task.id"
            :task="task"
            @edit="handleEditTask"
            @delete="handleDeleteTask"
          />
        </div>
      </div>
    </div>
  </div>
</template>
