<script setup lang="ts">
import Modal from '@/components/common/Modal.vue';
import Button from '@/components/common/Button.vue';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const closeModal = () => {
  emit('update:modelValue', false);
};

const openDocs = () => {
  window.open('/README.md', '_blank');
};
</script>

<template>
  <Modal :model-value="props.modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <template #header>
      <span>About Task Manager</span>
    </template>

    <div class="space-y-6">
      <!-- App Description -->
      <div>
        <h3 class="text-sm font-semibold text-gray-900 mb-2">Welcome to Task Manager</h3>
        <p class="text-sm text-gray-600">
          A personal productivity app to organize, track, and manage your tasks across multiple
          lists with real-time notifications for overdue and upcoming tasks.
        </p>
      </div>

      <!-- Key Features -->
      <div>
        <h3 class="text-sm font-semibold text-gray-900 mb-3">Key Features</h3>
        <ul class="space-y-2 text-sm text-gray-600">
          <li class="flex items-start gap-2">
            <span class="text-blue-600 font-bold mt-0.5">✓</span>
            <span><strong>Task Lists</strong> — Organize tasks into multiple projects</span>
          </li>
          <li class="flex items-start gap-2">
            <span class="text-blue-600 font-bold mt-0.5">✓</span>
            <span
              ><strong>Task Details</strong> — Add title, description, due date, and priority
              (Low/Medium/High)</span
            >
          </li>
          <li class="flex items-start gap-2">
            <span class="text-blue-600 font-bold mt-0.5">✓</span>
            <span><strong>Task Status</strong> — Track progress (To Do → In Progress → Done)</span>
          </li>
          <li class="flex items-start gap-2">
            <span class="text-blue-600 font-bold mt-0.5">✓</span>
            <span
              ><strong>Search & Filter</strong> — Find tasks by title, description, status, or
              priority</span
            >
          </li>
          <li class="flex items-start gap-2">
            <span class="text-blue-600 font-bold mt-0.5">✓</span>
            <span
              ><strong>Real-time Notifications</strong> — Get alerts for overdue and due-soon
              tasks</span
            >
          </li>
          <li class="flex items-start gap-2">
            <span class="text-blue-600 font-bold mt-0.5">✓</span>
            <span
              ><strong>Configurable Intervals</strong> — Adjust notification check frequency in
              Settings</span
            >
          </li>
        </ul>
      </div>

      <!-- Notifications Info -->
      <div class="bg-blue-50 border border-blue-200 rounded-lg p-3">
        <h4 class="text-sm font-semibold text-blue-900 mb-1">💬 Notifications</h4>
        <p class="text-sm text-blue-900">You'll receive notifications for:</p>
        <ul class="text-sm text-blue-900 mt-1 ml-4 space-y-0.5">
          <li>• <strong>Overdue</strong> — Tasks past their due date</li>
          <li>• <strong>Due Soon</strong> — Tasks due within the next 3 days</li>
        </ul>
        <p class="text-sm text-blue-900 mt-2">
          Customize how often you receive updates via Settings.
        </p>
      </div>

      <!-- Tips -->
      <div>
        <h3 class="text-sm font-semibold text-gray-900 mb-2">💡 Quick Tips</h3>
        <ul class="space-y-1 text-sm text-gray-600">
          <li>• Create separate lists to organize work, personal, and shopping tasks</li>
          <li>• Use priority levels to focus on what's important</li>
          <li>• Mark tasks as Done to remove them from notifications</li>
          <li>• Search for tasks by keyword to quickly find what you need</li>
        </ul>
      </div>

      <!-- Action Buttons -->
      <div class="flex gap-3 justify-end pt-2">
        <Button variant="secondary" @click="closeModal"> Close </Button>
        <Button @click="openDocs"> Read Full Docs </Button>
      </div>
    </div>
  </Modal>
</template>
