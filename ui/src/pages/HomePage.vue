<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useListStore } from '@/stores/listStore';
import { useTasks } from '@/composables/useTasks';
import MainLayout from '@/layouts/MainLayout.vue';
import ListSidebar from '@/components/lists/ListSidebar.vue';
import TaskFilterBar from '@/components/tasks/TaskFilterBar.vue';
import TaskList from '@/components/tasks/TaskList.vue';
import CreateListModal from '@/components/lists/CreateListModal.vue';
import CreateTaskForm from '@/components/tasks/CreateTaskForm.vue';

const listStore = useListStore();
const { fetchTasksByList } = useTasks();

const isCreateListOpen = ref(false);
const isCreateTaskOpen = ref(false);

const selectedListId = computed(() => listStore.selectedListId);

onMounted(() => {
  if (selectedListId.value) {
    fetchTasksByList(selectedListId.value);
  }
});

watch(selectedListId, (newListId) => {
  if (newListId) {
    fetchTasksByList(newListId);
  }
});
</script>

<template>
  <MainLayout>
    <!-- Sidebar with ListSidebar component -->
    <template #sidebar>
      <ListSidebar />
    </template>

    <!-- Main content area with filter bar and task list -->
    <div class="flex flex-col h-screen">
      <TaskFilterBar />
      <TaskList />
    </div>

    <!-- Create List Modal -->
    <CreateListModal :is-open="isCreateListOpen" @close="isCreateListOpen = false" />

    <!-- Create Task Modal -->
    <CreateTaskForm
      :is-open="isCreateTaskOpen"
      :list-id="selectedListId || ''"
      @close="isCreateTaskOpen = false"
    />
  </MainLayout>
</template>
