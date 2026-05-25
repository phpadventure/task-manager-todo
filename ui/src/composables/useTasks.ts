import { computed, ref } from 'vue';
import { useTaskStore, type TaskStatus } from '@/stores/taskStore';
import {
  taskApi,
  type TaskFilters,
  type CreateTaskRequest,
  type UpdateTaskRequest,
} from '@/api/taskApi';

export const useTasks = () => {
  const taskStore = useTaskStore();
  const isLoading = ref(false);
  const isCreating = ref(false);
  const isUpdating = ref(false);
  const isDeleting = ref(false);

  const tasks = computed(() => taskStore.tasks);
  const loading = computed(() => taskStore.loading);
  const error = computed(() => taskStore.error);
  const filters = computed(() => taskStore.filters);

  const getTasksByListId = computed(() => (listId: string) => taskStore.getTasksByListId(listId));

  const fetchTasksByList = async (listId: string, taskFilters?: TaskFilters) => {
    isLoading.value = true;
    try {
      const data = await taskApi.getTasksByList(listId, taskFilters);
      taskStore.setTasks(data);
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to fetch tasks');
    } finally {
      isLoading.value = false;
    }
  };

  const fetchAllTasks = async (taskFilters?: TaskFilters) => {
    isLoading.value = true;
    try {
      const data = await taskApi.getTasks(taskFilters);
      taskStore.setTasks(data);
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to fetch tasks');
    } finally {
      isLoading.value = false;
    }
  };

  const searchTasks = async (query: string) => {
    isLoading.value = true;
    try {
      const data = await taskApi.searchTasks(query);
      taskStore.setTasks(data);
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to search tasks');
    } finally {
      isLoading.value = false;
    }
  };

  const createTask = async (listId: string, data: CreateTaskRequest) => {
    isCreating.value = true;
    try {
      const newTask = await taskApi.createTask(listId, data);
      taskStore.addTask(newTask);
      return newTask;
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to create task');
      throw err;
    } finally {
      isCreating.value = false;
    }
  };

  const updateTask = async (listId: string, taskId: string, data: UpdateTaskRequest) => {
    isUpdating.value = true;
    try {
      const updated = await taskApi.updateTask(listId, taskId, data);
      taskStore.updateTask(taskId, updated);
      return updated;
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to update task');
      throw err;
    } finally {
      isUpdating.value = false;
    }
  };

  const updateTaskStatus = async (taskId: string, status: TaskStatus) => {
    try {
      const updated = await taskApi.updateTaskStatus(taskId, status);
      taskStore.updateTask(taskId, updated);
      return updated;
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to update task status');
      throw err;
    }
  };

  const deleteTask = async (listId: string, taskId: string) => {
    isDeleting.value = true;
    try {
      await taskApi.deleteTask(listId, taskId);
      taskStore.deleteTask(taskId);
    } catch (err) {
      taskStore.setError(err instanceof Error ? err.message : 'Failed to delete task');
      throw err;
    } finally {
      isDeleting.value = false;
    }
  };

  const setFilters = (newFilters: Partial<TaskFilters>) => {
    taskStore.setFilters(newFilters);
  };

  const clearFilters = () => {
    taskStore.clearFilters();
  };

  return {
    tasks,
    loading,
    error,
    filters,
    isLoading,
    isCreating,
    isUpdating,
    isDeleting,
    getTasksByListId,
    fetchTasksByList,
    fetchAllTasks,
    searchTasks,
    createTask,
    updateTask,
    updateTaskStatus,
    deleteTask,
    setFilters,
    clearFilters,
  };
};
