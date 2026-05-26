<script setup lang="ts">
import { ref, computed } from 'vue';
import { useTaskStore } from '@/stores/taskStore';
import Button from '@/components/common/Button.vue';

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
    dueDateFilter: selectedDueDate.value !== 'all' ? (selectedDueDate.value as any) : 'all',
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
  <div class="bg-white border-b border-gray-200 lg:border-none">
    <!-- Search Bar -->
    <div
      class="px-4 md:px-6 py-4 bg-gradient-to-r from-blue-50 via-white to-indigo-50 border-b border-gray-100"
    >
      <div class="flex items-center gap-3">
        <svg
          class="w-5 h-5 text-gray-400 flex-shrink-0 inline-block"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <input
          type="text"
          :value="searchQuery"
          placeholder="Search tasks by title or description..."
          class="flex-1 px-4 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all shadow-sm"
          @input="(e) => updateSearch((e.target as HTMLInputElement).value)"
        />
      </div>
    </div>

    <!-- Filters Section -->
    <div class="px-4 md:px-6 py-4">
      <!-- Desktop Grid Layout -->
      <div class="hidden md:grid grid-cols-3 gap-8">
        <!-- Status Filters -->
        <div>
          <p
            class="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-3 flex items-center gap-2"
          >
            <svg
              class="w-4 h-4 text-blue-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12l2 2 4-4"
              />
            </svg>
            Status
          </p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in statusOptions"
              :key="option.value"
              :class="[
                'px-4 py-2 text-sm font-medium rounded-full transition-all duration-200',
                selectedStatuses.includes(option.value)
                  ? 'bg-blue-600 text-white shadow-lg hover:shadow-xl hover:bg-blue-700'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-150 border border-gray-200 hover:border-gray-300',
              ]"
              @click="toggleStatus(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <!-- Priority Filters -->
        <div>
          <p
            class="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-3 flex items-center gap-2"
          >
            <svg
              class="w-4 h-4 text-orange-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
            Priority
          </p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in priorityOptions"
              :key="option.value"
              :class="[
                'px-4 py-2 text-sm font-medium rounded-full transition-all duration-200',
                selectedPriorities.includes(option.value)
                  ? 'bg-orange-600 text-white shadow-lg hover:shadow-xl hover:bg-orange-700'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-150 border border-gray-200 hover:border-gray-300',
              ]"
              @click="togglePriority(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <!-- Due Date Filter -->
        <div>
          <p
            class="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-3 flex items-center gap-2"
          >
            <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            Due Date
          </p>
          <select
            :value="selectedDueDate"
            class="w-full px-4 py-2.5 text-sm bg-white border border-gray-200 rounded-lg hover:border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all cursor-pointer shadow-sm font-medium"
            @change="updateDueDate(($event.target as HTMLSelectElement).value)"
          >
            <option v-for="option in dueDateOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
      </div>

      <!-- Mobile Collapsible Filters -->
      <div class="md:hidden space-y-4">
        <!-- Status -->
        <div>
          <p
            class="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-2.5 flex items-center gap-2"
          >
            <svg
              class="w-4 h-4 text-blue-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12l2 2 4-4"
              />
            </svg>
            Status
          </p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in statusOptions"
              :key="option.value"
              :class="[
                'px-3 py-1.5 text-xs font-medium rounded-full transition-all duration-200',
                selectedStatuses.includes(option.value)
                  ? 'bg-blue-600 text-white shadow-md hover:shadow-lg hover:bg-blue-700'
                  : 'bg-gray-100 text-gray-700 border border-gray-200 hover:bg-gray-150',
              ]"
              @click="toggleStatus(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <!-- Priority -->
        <div>
          <p
            class="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-2.5 flex items-center gap-2"
          >
            <svg
              class="w-4 h-4 text-orange-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
            Priority
          </p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in priorityOptions"
              :key="option.value"
              :class="[
                'px-3 py-1.5 text-xs font-medium rounded-full transition-all duration-200',
                selectedPriorities.includes(option.value)
                  ? 'bg-orange-600 text-white shadow-md hover:shadow-lg hover:bg-orange-700'
                  : 'bg-gray-100 text-gray-700 border border-gray-200 hover:bg-gray-150',
              ]"
              @click="togglePriority(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <!-- Due Date -->
        <div>
          <p
            class="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-2.5 flex items-center gap-2"
          >
            <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            Due Date
          </p>
          <select
            :value="selectedDueDate"
            class="w-full px-3 py-2.5 text-sm bg-white border border-gray-200 rounded-lg hover:border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all cursor-pointer shadow-sm font-medium"
            @change="updateDueDate(($event.target as HTMLSelectElement).value)"
          >
            <option v-for="option in dueDateOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
      </div>

      <!-- Filter Status & Clear -->
      <div
        v-if="activeFilterCount > 0"
        class="flex items-center justify-between mt-4 pt-4 border-t border-gray-100"
      >
        <div class="flex items-center gap-2">
          <div class="w-2 h-2 rounded-full bg-blue-600"></div>
          <span class="text-sm font-medium text-gray-700">
            {{ activeFilterCount }} filter<span v-if="activeFilterCount !== 1">s</span> active
          </span>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          class="text-xs hover:text-gray-900"
          @click="clearFilters"
        >
          ✕ Clear all
        </Button>
      </div>
    </div>
  </div>
</template>
