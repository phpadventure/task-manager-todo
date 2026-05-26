<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useListStore } from '@/stores/listStore';
import { useTaskStore } from '@/stores/taskStore';
import { useTasks } from '@/composables/useTasks';
import MainLayout from '@/layouts/MainLayout.vue';
import ListSidebar from '@/components/lists/ListSidebar.vue';
import TaskFilterBar from '@/components/tasks/TaskFilterBar.vue';
import TaskList from '@/components/tasks/TaskList.vue';
import CreateListModal from '@/components/lists/CreateListModal.vue';
import CreateTaskForm from '@/components/tasks/CreateTaskForm.vue';
import EditListModal from '@/components/lists/EditListModal.vue';
import EditTaskForm from '@/components/tasks/EditTaskForm.vue';
import Modal from '@/components/common/Modal.vue';
import Button from '@/components/common/Button.vue';

const listStore = useListStore();
const taskStore = useTaskStore();
const { fetchTasksByList, deleteTask } = useTasks();

const isCreateListOpen = ref(false);
const isCreateTaskOpen = ref(false);
const isEditListOpen = ref(false);
const isEditTaskOpen = ref(false);
const isDeleteConfirmOpen = ref(false);
const editingTaskId = ref<string | null>(null);
const deletingTaskId = ref<string | null>(null);

const selectedListId = computed(() => listStore.selectedListId);

const selectedList = computed(() => {
  if (!selectedListId.value) return null;
  return listStore.getListById(selectedListId.value);
});

const editingTask = computed(() => {
  if (!editingTaskId.value) return null;
  return taskStore.getTaskById(editingTaskId.value);
});

const openEditList = () => {
  isEditListOpen.value = true;
};

const openEditTask = (taskId: string) => {
  editingTaskId.value = taskId;
  isEditTaskOpen.value = true;
};

const closeEditTask = () => {
  isEditTaskOpen.value = false;
  editingTaskId.value = null;
};

const openDeleteConfirm = (taskId: string) => {
  deletingTaskId.value = taskId;
  isDeleteConfirmOpen.value = true;
};

const confirmDelete = async () => {
  if (deletingTaskId.value && selectedListId.value) {
    await deleteTask(selectedListId.value, deletingTaskId.value);
    isDeleteConfirmOpen.value = false;
    deletingTaskId.value = null;
  }
};

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
      <ListSidebar @edit-list="openEditList" />
    </template>

    <!-- Main content area with filter bar and task list -->
    <div class="flex flex-col h-screen">
      <TaskFilterBar />
      <TaskList
        @edit-task="openEditTask"
        @delete-task="openDeleteConfirm"
        @create-task="isCreateTaskOpen = true"
      />
    </div>

    <!-- Create List Modal -->
    <CreateListModal :is-open="isCreateListOpen" @close="isCreateListOpen = false" />

    <!-- Edit List Modal -->
    <EditListModal
      :is-open="isEditListOpen"
      :list-id="selectedListId || ''"
      :list-title="selectedList?.title || ''"
      @close="isEditListOpen = false"
    />

    <!-- Create Task Modal -->
    <CreateTaskForm
      :is-open="isCreateTaskOpen"
      :list-id="selectedListId || ''"
      @close="isCreateTaskOpen = false"
    />

    <!-- Edit Task Modal -->
    <EditTaskForm
      v-if="editingTaskId && editingTask"
      :is-open="isEditTaskOpen"
      :task-id="editingTaskId"
      :list-id="selectedListId || ''"
      :title="editingTask.title"
      :description="editingTask.description"
      :due-date="editingTask.dueDate"
      :priority="editingTask.priority"
      @close="closeEditTask"
    />

    <!-- Delete Confirmation Modal -->
    <Modal :model-value="isDeleteConfirmOpen" @update:model-value="isDeleteConfirmOpen = $event">
      <template #header>
        <h2 class="text-lg font-bold text-red-600">Delete Task?</h2>
      </template>
      <p class="text-gray-700 mb-4">
        Are you sure you want to delete this task? This action cannot be undone.
      </p>
      <template #footer>
        <Button variant="ghost" @click="isDeleteConfirmOpen = false">Cancel</Button>
        <Button variant="danger" @click="confirmDelete">Delete</Button>
      </template>
    </Modal>
  </MainLayout>
</template>
