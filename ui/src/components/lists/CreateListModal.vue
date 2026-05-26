<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useForm } from '@/composables/useForm';
import { useLists } from '@/composables/useLists';
import { validateListTitle } from '@/utils/validation';
import { getErrorMessage } from '@/utils/apiErrorHandler';
import Button from '@/components/common/Button.vue';
import Input from '@/components/common/Input.vue';

interface Props {
  isOpen: boolean;
}

const props = withDefaults(defineProps<Props>(), {});

const emit = defineEmits<{
  close: [];
}>();

const { createList, isCreating } = useLists();
const { values, errors, touched, isDirty, isValid, setValues, handleSubmit } = useForm({
  title: '',
});
const submitError = ref<string | null>(null);
const fieldErrors = ref<Record<string, string>>({});

watch(
  () => props.isOpen,
  (isOpen) => {
    if (isOpen) {
      setValues({ title: '' });
    }
  }
);

const onSubmit = handleSubmit(async (formData) => {
  submitError.value = null;
  fieldErrors.value = {};
  try {
    await createList(formData.title);
    emit('close');
  } catch (error) {
    const errorInfo = getErrorMessage(error);
    submitError.value = errorInfo.message;
    if (errorInfo.fieldErrors) {
      fieldErrors.value = errorInfo.fieldErrors;
    }
  }
});

const onTitleBlur = () => {
  const error = validateListTitle(values.title);
  if (error) {
    touched.title = true;
  }
};

const canSubmit = computed(() => values.title.trim().length > 0 && !isCreating.value);
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black bg-opacity-50 z-40 flex items-center justify-center"
  >
    <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
      <h2 class="text-xl font-bold mb-4">Create New List</h2>

      <form class="space-y-4" @submit="onSubmit">
        <!-- Error Alert -->
        <div v-if="submitError" class="bg-red-50 border border-red-200 rounded-lg p-3">
          <p class="text-sm text-red-800">{{ submitError }}</p>
        </div>

        <div>
          <Input
            v-model="values.title"
            type="text"
            label="List Title"
            placeholder="e.g., Work, Personal, Shopping"
            :error="fieldErrors.title || (touched.title ? errors.title : null)"
            @blur="onTitleBlur"
          />
        </div>

        <div class="flex gap-2 justify-end pt-4">
          <Button type="button" variant="secondary" :disabled="isCreating" @click="emit('close')">
            Cancel
          </Button>
          <Button type="submit" variant="primary" :disabled="!canSubmit" :loading="isCreating">
            Create List
          </Button>
        </div>
      </form>
    </div>
  </div>
</template>
