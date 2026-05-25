<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useTaskStore } from '@/stores/taskStore';
import Button from '@/components/common/Button.vue';
import Input from '@/components/common/Input.vue';

const taskStore = useTaskStore();

const searchQuery = ref('');
const selectedStatuses = ref<string[]>([]);
const selectedPriorities = ref<string[]>([]);
const selectedDueDate = ref('all');

const statusOptions = [
  { value: 'TO_DO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'DONE', label: 'Done' },
];

const priorityOptions = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
];

const dueDateOptions = [
  { value: 'all', label: 'All' },
  { value: 'overdue', label: 'Overdue' },
  { value: 'today', label: 'Today' },
  { value: 'next_7_days', label: 'Next 7 Days' },
];

const activeFilterCount = computed(() => {
  return (
    (searchQuery.value ? 1 : 0) +
    selectedStatuses.value.length +
    selectedPriorities.value.length +
    (selectedDueDate.value !== 'all' ? 1 : 0)
  );
});

// Debounce search (300ms)
const searchTimeout = ref<NodeJS.Timeout>();
const updateSearch = (value: string) => {
  searchQuery.value = value;
  clearTimeout(searchTimeout.value);
  searchTimeout.value = setTimeout(() => {
    applyFilters();
  }, 300);
};

const toggleStatus = (status: string) => {
  const idx = selectedStatuses.value.indexOf(status);
  if (idx > -1) {
    selectedStatuses.value.splice(idx, 1);
  } else {
    selectedStatuses.value.push(status);
  }
  applyFilters();
};

const togglePriority = (priority: string) => {
  const idx = selectedPriorities.value.indexOf(priority);
  if (idx > -1) {
    selectedPriorities.value.splice(idx, 1);
  } else {
    selectedPriorities.value.push(priority);
  }
  applyFilters();
};

const updateDueDate = (value: string) => {
  selectedDueDate.value = value;
  applyFilters();
};

const applyFilters = () => {
  taskStore.setFilters({
    searchQuery: searchQuery.value,
    status: selectedStatuses.value.length > 0 ? selectedStatuses.value : undefined,
    priority: selectedPriorities.value.length > 0 ? selectedPriorities.value : undefined,
    dueDate: selectedDueDate.value !== 'all' ? (selectedDueDate.value as any) : undefined,
  });
};

const clearFilters = () => {
  searchQuery.value = '';
  selectedStatuses.value = [];
  selectedPriorities.value = [];
  selectedDueDate.value = 'all';
  taskStore.clearFilters();
};
</script>

<template>
  <div class="bg-base-100 border-b border-base-300 p-4">
    <div class="space-y-4">
      <!-- Search -->
      <div>
        <Input
          type="text"
          placeholder="Search tasks..."
          :value="searchQuery"
          @input="(e) => updateSearch((e.target as HTMLInputElement).value)"
        />
      </div>

      <!-- Status Filters -->
      <div>
        <label class="block text-sm font-medium mb-2">Status</label>
        <div class="flex flex-wrap gap-2">
          <label
            v-for="option in statusOptions"
            :key="option.value"
            class="flex items-center gap-2 cursor-pointer"
          >
            <input
              type="checkbox"
              :checked="selectedStatuses.includes(option.value)"
              @change="toggleStatus(option.value)"
              class="checkbox checkbox-sm"
            />
            <span class="text-sm">{{ option.label }}</span>
          </label>
        </div>
      </div>

      <!-- Priority Filters -->
      <div>
        <label class="block text-sm font-medium mb-2">Priority</label>
        <div class="flex flex-wrap gap-2">
          <label
            v-for="option in priorityOptions"
            :key="option.value"
            class="flex items-center gap-2 cursor-pointer"
          >
            <input
              type="checkbox"
              :checked="selectedPriorities.includes(option.value)"
              @change="togglePriority(option.value)"
              class="checkbox checkbox-sm"
            />
            <span class="text-sm">{{ option.label }}</span>
          </label>
        </div>
      </div>

      <!-- Due Date Filter -->
      <div>
        <label class="block text-sm font-medium mb-2">Due Date</label>
        <select
          :value="selectedDueDate"
          @change="updateDueDate(($event.target as HTMLSelectElement).value)"
          class="select select-bordered select-sm w-full"
        >
          <option v-for="option in dueDateOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>

      <!-- Action Buttons -->
      <div class="flex items-center justify-between pt-2">
        <span v-if="activeFilterCount > 0" class="badge badge-primary"
          >{{ activeFilterCount }} active</span
        >
        <span v-else class="text-sm text-base-500">No filters applied</span>
        <Button
          v-if="activeFilterCount > 0"
          type="button"
          variant="ghost"
          size="sm"
          @click="clearFilters"
        >
          Clear Filters
        </Button>
      </div>
    </div>
  </div>
</template>
