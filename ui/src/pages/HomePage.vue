<template>
  <MainLayout>
    <template #sidebar>
      <div class="p-4">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-bold">Lists</h2>
          <button
            class="p-1 text-gray-600 hover:bg-gray-100 rounded"
            @click="showCreateListModal = true"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v16m8-8H4"
              />
            </svg>
          </button>
        </div>

        <div v-if="listsLoading" class="text-center py-4">
          <div
            class="inline-block w-5 h-5 border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin"
          />
        </div>

        <ul v-else class="space-y-1">
          <li
            v-for="list in lists"
            :key="list.id"
            :class="[
              'p-2 rounded cursor-pointer hover:bg-gray-100',
              { 'bg-blue-50 text-blue-700': selectedListId === list.id },
            ]"
            @click="selectList(list.id)"
          >
            {{ list.title }}
          </li>
        </ul>
      </div>
    </template>

    <div class="p-8">
      <div v-if="!selectedListId" class="text-center text-gray-500 py-12">
        <p>Select a list to view tasks</p>
      </div>

      <div v-else class="space-y-4">
        <div class="flex items-center justify-between">
          <h1 class="text-2xl font-bold">{{ selectedList?.title || 'Tasks' }}</h1>
          <button
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            @click="showCreateTaskModal = true"
          >
            + New Task
          </button>
        </div>

        <div v-if="tasksLoading" class="text-center py-12">
          <div
            class="inline-block w-6 h-6 border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin"
          />
        </div>

        <div v-else-if="taskList.length === 0" class="text-center py-12 text-gray-500">
          <p>No tasks in this list</p>
        </div>

        <ul v-else class="space-y-2">
          <li v-for="task in taskList" :key="task.id" class="card p-4 hover:shadow-md transition">
            <div class="flex items-start justify-between">
              <div>
                <h3 class="font-semibold text-gray-900">{{ task.title }}</h3>
                <p v-if="task.description" class="text-sm text-gray-600 mt-1">
                  {{ task.description }}
                </p>
                <div class="flex gap-2 mt-2">
                  <Badge type="success">{{ task.status }}</Badge>
                  <Badge type="warning">{{ task.priority }}</Badge>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import MainLayout from '@/layouts/MainLayout.vue';
import Badge from '@/components/common/Badge.vue';
import { useLists } from '@/composables/useLists';
import { useTasks } from '@/composables/useTasks';

const showCreateListModal = ref(false);
const showCreateTaskModal = ref(false);

const { lists, selectedListId, listsLoading: loading, fetchLists, selectList } = useLists();
const { getTasksByListId, tasksLoading, fetchTasksByList } = useTasks();

const selectedList = computed(() => lists.value.find((l) => l.id === selectedListId.value));

const taskList = computed(() =>
  selectedListId.value ? getTasksByListId.value(selectedListId.value) : []
);

const listsLoading = computed(() => loading.value);

onMounted(async () => {
  await fetchLists();
});
</script>
