<script setup lang="ts">
import { computed, ref } from 'vue';
import { useForm } from '@/composables/useForm';
import { useTasks } from '@/composables/useTasks';
import { validateTaskTitle, validateTaskDescription, validateDueDate } from '@/utils/validation';
import { isConflictError, getErrorMessage } from '@/utils/apiErrorHandler';
import Button from '@/components/common/Button.vue';
import Input from '@/components/common/Input.vue';

interface Props {
  isOpen: boolean;
  listId: string;
}

const props = withDefaults(defineProps<Props>(), {});

const emit = defineEmits<{
  close: [];
}>();

const { createTask, isCreating } = useTasks();
const { values, errors, touched, setValues, handleSubmit } = useForm({
  title: '',
  description: '',
  dueDate: '',
  priority: 'MEDIUM',
});
const submitError = ref<string | null>(null);
const fieldErrors = ref<Record<string, string>>({});

const priorityOptions = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
];

const canSubmit = computed(() => values.title.trim().length > 0 && !isCreating.value);

const validateTitle = () => {
  const error = validateTaskTitle(values.title);
  if (error) {
    touched.title = true;
  }
};

const validateDesc = () => {
  const error = validateTaskDescription(values.description);
  if (error) {
    touched.description = true;
  }
};

const validateDueDateField = () => {
  const error = validateDueDate(values.dueDate);
  if (error) {
    touched.dueDate = true;
  }
};

const onSubmit = handleSubmit(async (formData) => {
  submitError.value = null;
  fieldErrors.value = {};
  try {
    await createTask(props.listId, {
      title: formData.title,
      description: formData.description || undefined,
      dueDate: formData.dueDate || undefined,
      priority: formData.priority as 'LOW' | 'MEDIUM' | 'HIGH',
    });
    setValues({
      title: '',
      description: '',
      dueDate: '',
      priority: 'MEDIUM',
    });
    emit('close');
  } catch (error) {
    const errorInfo = getErrorMessage(error);
    submitError.value = errorInfo.message;
    if (errorInfo.fieldErrors) {
      fieldErrors.value = errorInfo.fieldErrors;
    }
  }
});
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black bg-opacity-50 z-40 flex items-center justify-center"
  >
    <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
      <h2 class="text-xl font-bold mb-4">Create New Task</h2>

      <form class="space-y-4" @submit="onSubmit">
        <!-- Error Alert -->
        <div v-if="submitError" class="bg-red-50 border border-red-200 rounded-lg p-3">
          <p class="text-sm text-red-800">{{ submitError }}</p>
        </div>

        <!-- Title -->
        <div>
          <Input
            v-model="values.title"
            type="text"
            label="Task Title"
            placeholder="e.g., Buy groceries"
            :error="fieldErrors.title || (touched.title ? errors.title : null)"
            @blur="validateTitle"
          />
        </div>

        <!-- Description -->
        <div>
          <label class="block text-sm font-medium mb-1">Description (optional)</label>
          <textarea
            v-model="values.description"
            placeholder="Add details..."
            class="textarea textarea-bordered w-full text-sm"
            rows="3"
            @blur="validateDesc"
          ></textarea>
          <p
            v-if="fieldErrors.description || (touched.description && errors.description)"
            class="text-error text-xs mt-1"
          >
            {{ fieldErrors.description || errors.description }}
          </p>
        </div>

        <!-- Due Date -->
        <div>
          <Input
            v-model="values.dueDate"
            type="date"
            label="Due Date (optional)"
            :error="fieldErrors.dueDate || (touched.dueDate ? errors.dueDate : null)"
            @blur="validateDueDateField"
          />
        </div>

        <!-- Priority -->
        <div>
          <label class="block text-sm font-medium mb-2">Priority</label>
          <select v-model="values.priority" class="select select-bordered select-sm w-full">
            <option v-for="option in priorityOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>

        <!-- Actions -->
        <div class="flex gap-2 justify-end pt-4">
          <Button type="button" variant="secondary" :disabled="isCreating" @click="emit('close')">
            Cancel
          </Button>
          <Button type="submit" variant="primary" :disabled="!canSubmit" :loading="isCreating">
            Create Task
          </Button>
        </div>
      </form>
    </div>
  </div>
</template>
