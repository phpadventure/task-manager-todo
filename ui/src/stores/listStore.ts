import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

interface TaskList {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export const useListStore = defineStore('list', () => {
  const lists = ref<TaskList[]>([]);
  const selectedListId = ref<string | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const selectedList = computed(() =>
    selectedListId.value ? lists.value.find((l) => l.id === selectedListId.value) : null
  );

  const getListById = (id: string) => lists.value.find((l) => l.id === id);

  const setLists = (newLists: TaskList[]) => {
    lists.value = newLists;
    error.value = null;
  };

  const setSelectedList = (id: string | null) => {
    selectedListId.value = id;
  };

  const addList = (list: TaskList) => {
    lists.value.push(list);
  };

  const updateList = (id: string, updates: Partial<TaskList>) => {
    const index = lists.value.findIndex((l) => l.id === id);
    if (index !== -1) {
      lists.value[index] = { ...lists.value[index], ...updates };
    }
  };

  const deleteList = (id: string) => {
    lists.value = lists.value.filter((l) => l.id !== id);
    if (selectedListId.value === id) {
      selectedListId.value = null;
    }
  };

  const setLoading = (state: boolean) => {
    loading.value = state;
  };

  const setError = (message: string | null) => {
    error.value = message;
  };

  return {
    lists,
    selectedListId,
    loading,
    error,
    selectedList,
    getListById,
    setLists,
    setSelectedList,
    addList,
    updateList,
    deleteList,
    setLoading,
    setError,
  };
});
