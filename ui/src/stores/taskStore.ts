import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export type TaskStatus = 'TO_DO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

interface Task {
  id: string;
  listId: string;
  userId: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

interface TaskFilters {
  status?: TaskStatus[];
  priority?: TaskPriority[];
  dueDateFilter?: 'overdue' | 'today' | 'next_7_days' | 'all';
  searchQuery?: string;
}

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([]);
  const filters = ref<TaskFilters>({
    dueDateFilter: 'all',
  });
  const loading = ref(false);
  const error = ref<string | null>(null);

  const getTasksByListId = computed(() => (listId: string) => {
    let filtered = tasks.value.filter((t) => t.listId === listId);

    if (filters.value.status?.length) {
      filtered = filtered.filter((t) => filters.value.status?.includes(t.status));
    }

    if (filters.value.priority?.length) {
      filtered = filtered.filter((t) => filters.value.priority?.includes(t.priority));
    }

    if (filters.value.searchQuery) {
      const q = filters.value.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (t) => t.title.toLowerCase().includes(q) || t.description?.toLowerCase().includes(q)
      );
    }

    return filtered;
  });

  const getTaskById = (id: string) => tasks.value.find((t) => t.id === id);

  const setTasks = (newTasks: Task[]) => {
    tasks.value = newTasks;
    error.value = null;
  };

  const setFilters = (newFilters: Partial<TaskFilters>) => {
    filters.value = { ...filters.value, ...newFilters };
  };

  const clearFilters = () => {
    filters.value = { dueDateFilter: 'all' };
  };

  const addTask = (task: Task) => {
    tasks.value.push(task);
  };

  const updateTask = (id: string, updates: Partial<Task>) => {
    const index = tasks.value.findIndex((t) => t.id === id);
    if (index !== -1) {
      tasks.value[index] = { ...tasks.value[index], ...updates };
    }
  };

  const deleteTask = (id: string) => {
    tasks.value = tasks.value.filter((t) => t.id !== id);
  };

  const setLoading = (state: boolean) => {
    loading.value = state;
  };

  const setError = (message: string | null) => {
    error.value = message;
  };

  return {
    tasks,
    filters,
    loading,
    error,
    getTasksByListId,
    getTaskById,
    setTasks,
    setFilters,
    clearFilters,
    addTask,
    updateTask,
    deleteTask,
    setLoading,
    setError,
  };
});
