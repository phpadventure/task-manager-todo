import { computed, ref } from 'vue';
import { useListStore } from '@/stores/listStore';
import { listApi, CreateListRequest, UpdateListRequest } from '@/api/listApi';
import { useToastError } from '@/composables/useToastError';

export const useLists = () => {
  const listStore = useListStore();
  const { showErrorWithContext } = useToastError();
  const isCreating = ref(false);
  const isUpdating = ref(false);
  const isDeleting = ref(false);

  const lists = computed(() => listStore.lists);
  const selectedList = computed(() => listStore.selectedList);
  const loading = computed(() => listStore.loading);
  const error = computed(() => listStore.error);

  const fetchLists = async () => {
    listStore.setLoading(true);
    try {
      const data = await listApi.getLists();
      listStore.setLists(data);
    } catch (err) {
      listStore.setError(err instanceof Error ? err.message : 'Failed to fetch lists');
    } finally {
      listStore.setLoading(false);
    }
  };

  const createList = async (title: string) => {
    isCreating.value = true;
    try {
      const newList = await listApi.createList({ title });
      listStore.addList(newList);
      return newList;
    } catch (err) {
      listStore.setError(err instanceof Error ? err.message : 'Failed to create list');
      showErrorWithContext(err, 'list', { fallback: 'Failed to create list' });
      throw err;
    } finally {
      isCreating.value = false;
    }
  };

  const updateList = async (id: string, title: string) => {
    isUpdating.value = true;
    try {
      const updated = await listApi.updateList(id, { title });
      listStore.updateList(id, updated);
      return updated;
    } catch (err) {
      listStore.setError(err instanceof Error ? err.message : 'Failed to update list');
      showErrorWithContext(err, 'list', { fallback: 'Failed to update list' });
      throw err;
    } finally {
      isUpdating.value = false;
    }
  };

  const deleteList = async (id: string) => {
    isDeleting.value = true;
    try {
      await listApi.deleteList(id);
      listStore.deleteList(id);
    } catch (err) {
      listStore.setError(err instanceof Error ? err.message : 'Failed to delete list');
      showErrorWithContext(err, 'list', { fallback: 'Failed to delete list' });
      throw err;
    } finally {
      isDeleting.value = false;
    }
  };

  const selectList = (id: string) => {
    listStore.setSelectedList(id);
  };

  return {
    lists,
    selectedList,
    loading,
    error,
    isCreating,
    isUpdating,
    isDeleting,
    fetchLists,
    createList,
    updateList,
    deleteList,
    selectList,
  };
};
