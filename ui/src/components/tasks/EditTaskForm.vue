<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick } from 'vue';
import { useForm } from '@/composables/useForm';
import { useTasks } from '@/composables/useTasks';
import { validateTaskTitle, validateTaskDescription, validateDueDate } from '@/utils/validation';
import { getErrorMessage } from '@/utils/apiErrorHandler';
import Modal from '@/components/common/Modal.vue';
import Button from '@/components/common/Button.vue';
import Input from '@/components/common/Input.vue';

interface Props {
  isOpen: boolean;
  taskId?: string;
  listId?: string;
  title?: string;
  description?: string;
  dueDate?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
}

const props = withDefaults(defineProps<Props>(), {
  priority: 'MEDIUM',
});

const emit = defineEmits<{
  close: [];
}>();

const { updateTask, deleteTask, isUpdating, isDeleting } = useTasks();
const { values, errors, touched, setValues, handleSubmit } = useForm({
  title: '',
  description: '',
  dueDate: '',
  priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH',
});

const priorityOptions = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
];

const showDeleteConfirm = ref(false);
const submitError = ref<string | null>(null);
const deleteError = ref<string | null>(null);
const fieldErrors = ref<Record<string, string>>({});

const populateForm = async () => {
  await nextTick();
  setValues({
    title: props.title || '',
    description: props.description || '',
    dueDate: props.dueDate || '',
    priority: props.priority || 'MEDIUM',
  });
};

onMounted(() => {
  populateForm();
});

watch(
  () => props.isOpen,
  (isOpen) => {
    if (isOpen) {
      populateForm();
      showDeleteConfirm.value = false;
    }
  }
);

watch(
  () => [props.title, props.description, props.dueDate, props.priority],
  () => {
    if (props.isOpen) {
      populateForm();
    }
  }
);

const onSubmit = handleSubmit(async (formData) => {
  if (!props.taskId) return;
  submitError.value = null;
  fieldErrors.value = {};
  try {
    await updateTask(props.listId, props.taskId, {
      title: formData.title,
      description: formData.description || undefined,
      dueDate: formData.dueDate || undefined,
      priority: formData.priority as 'LOW' | 'MEDIUM' | 'HIGH',
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

const onDelete = async () => {
  if (!props.taskId || !props.listId) return;
  deleteError.value = null;
  try {
    await deleteTask(props.listId, props.taskId);
    emit('close');
  } catch (error) {
    deleteError.value = getErrorMessage(error).message;
    showDeleteConfirm.value = false;
  }
};

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

const canSubmit = computed(() => values.title.trim().length > 0 && !isUpdating.value);
const isLoading = computed(() => isUpdating.value || isDeleting.value);
</script>

<template>
  <Modal :model-value="isOpen" @update:model-value="(val) => !val && emit('close')">
    <template #header>
      <h2 class="text-xl font-bold">Edit Task</h2>
    </template>

    <div v-if="!showDeleteConfirm" class="space-y-4">
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
          class="form-input resize-none h-24"
          :class="{
            'border-red-500 focus:ring-red-500':
              fieldErrors.description || (touched.description && errors.description),
          }"
          @blur="validateDesc"
        />
        <p
          v-if="fieldErrors.description || (touched.description && errors.description)"
          class="mt-1 text-sm text-red-600"
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
        <label class="block text-sm font-medium mb-1">Priority</label>
        <select v-model="values.priority" class="form-input">
          <option v-for="opt in priorityOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
    </div>

    <!-- Delete Confirmation -->
    <div v-else class="space-y-4">
      <!-- Delete Error Alert -->
      <div v-if="deleteError" class="bg-red-50 border border-red-200 rounded-lg p-3">
        <p class="text-sm text-red-800">{{ deleteError }}</p>
      </div>

      <div class="bg-red-50 border border-red-200 rounded-lg p-4">
        <p class="font-medium text-red-900">Delete this task?</p>
        <p class="text-sm text-red-700 mt-1">This action cannot be undone.</p>
      </div>
    </div>

    <template #footer>
      <div v-if="!showDeleteConfirm" class="flex gap-2 w-full">
        <Button
          type="button"
          variant="danger"
          :disabled="isLoading"
          class="mr-auto"
          @click="showDeleteConfirm = true"
        >
          Delete
        </Button>
        <Button type="button" variant="secondary" :disabled="isLoading" @click="emit('close')">
          Cancel
        </Button>
        <Button
          type="submit"
          variant="primary"
          :disabled="!canSubmit"
          :loading="isUpdating"
          @click="onSubmit"
        >
          Update Task
        </Button>
      </div>
      <div v-else class="flex gap-2 w-full">
        <Button
          type="button"
          variant="secondary"
          :disabled="isDeleting"
          @click="showDeleteConfirm = false"
        >
          Keep Task
        </Button>
        <Button
          type="button"
          variant="danger"
          :disabled="isDeleting"
          :loading="isDeleting"
          @click="onDelete"
        >
          Delete Permanently
        </Button>
      </div>
    </template>
  </Modal>
</template>
