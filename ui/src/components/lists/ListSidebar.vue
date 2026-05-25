<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useListStore } from '@/stores/listStore';
import { useLists } from '@/composables/useLists';
import { useLocalStorage } from '@/composables/useLocalStorage';
import Button from '@/components/common/Button.vue';
import CreateListModal from './CreateListModal.vue';

const listStore = useListStore();
const { fetchLists, isLoading } = useLists();
const { load: loadPersistedListId } = useLocalStorage<string | null>('selectedListId', null);

const isModalOpen = ref(false);

onMounted(async () => {
  await fetchLists();
  const persistedListId = loadPersistedListId();
  if (persistedListId && listStore.lists.some((l) => l.id === persistedListId)) {
    listStore.setSelectedList(persistedListId);
  } else if (listStore.lists.length > 0) {
    listStore.setSelectedList(listStore.lists[0].id);
  }
});

const selectedListId = computed(() => listStore.selectedListId);

const selectList = (listId: string) => {
  listStore.setSelectedList(listId);
};

const toggleModal = () => {
  isModalOpen.value = !isModalOpen.value;
};
</script>

<template>
  <div class="h-full bg-white flex flex-col">
    <!-- Header (hidden on mobile since it's in MainLayout) -->
    <div class="hidden lg:block p-4 md:p-5 border-b border-gray-200">
      <h1 class="text-lg font-bold text-gray-900">My Lists</h1>
    </div>

    <!-- Lists -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="isLoading" class="p-4 text-center text-sm text-gray-500">Loading lists...</div>
      <div v-else-if="listStore.lists.length === 0" class="p-4 text-center text-sm text-gray-500">
        No lists yet. Create one to get started!
      </div>
      <nav v-else class="p-3 space-y-1">
        <button
          v-for="list in listStore.lists"
          :key="list.id"
          :class="[
            'w-full text-left px-4 py-3 rounded-lg text-sm font-medium transition-colors duration-150',
            selectedListId === list.id
              ? 'bg-blue-600 text-white shadow-md hover:bg-blue-700'
              : 'text-gray-700 hover:bg-gray-100',
          ]"
          @click="selectList(list.id)"
        >
          {{ list.title }}
        </button>
      </nav>
    </div>

    <!-- Create Button -->
    <div class="p-3 border-t border-gray-200">
      <Button type="button" variant="primary" class="w-full" @click="toggleModal">
        + New List
      </Button>
    </div>

    <!-- Modal -->
    <CreateListModal :is-open="isModalOpen" @close="isModalOpen = false" />
  </div>
</template>
