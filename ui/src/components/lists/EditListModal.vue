<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useForm } from '@/composables/useForm';
import { useLists } from '@/composables/useLists';
import { validateListTitle } from '@/utils/validation';
import Modal from '@/components/common/Modal.vue';
import Button from '@/components/common/Button.vue';
import Input from '@/components/common/Input.vue';

interface Props {
  isOpen: boolean;
  listId?: string;
  listTitle?: string;
}

const props = withDefaults(defineProps<Props>(), {});

const emit = defineEmits<{
  close: [];
}>();

const { updateList, deleteList, isUpdating, isDeleting } = useLists();
const { values, errors, touched, setValues, handleSubmit } = useForm({
  title: '',
});
const showDeleteConfirm = ref(false);

watch(
  () => props.isOpen,
  (isOpen) => {
    if (isOpen && props.listTitle) {
      setValues({ title: props.listTitle });
      showDeleteConfirm.value = false;
    }
  }
);

const onSubmit = handleSubmit(async (formData) => {
  if (!props.listId) return;
  await updateList(props.listId, formData.title);
  emit('close');
});

const onDelete = async () => {
  if (!props.listId) return;
  await deleteList(props.listId);
  emit('close');
};

const onTitleBlur = () => {
  const error = validateListTitle(values.title);
  if (error) {
    touched.title = true;
  }
};

const canSubmit = computed(() => values.title.trim().length > 0 && !isUpdating.value);
const isLoading = computed(() => isUpdating.value || isDeleting.value);
</script>

<template>
  <Modal :model-value="isOpen" @update:model-value="(val) => !val && emit('close')">
    <template #header>
      <h2 class="text-xl font-bold">Edit List</h2>
    </template>

    <div v-if="!showDeleteConfirm" class="space-y-4">
      <!-- Title -->
      <div>
        <Input
          v-model="values.title"
          type="text"
          label="List Title"
          placeholder="e.g., Work, Personal, Shopping"
          :error="touched.title ? errors.title : null"
          @blur="onTitleBlur"
        />
      </div>
    </div>

    <!-- Delete Confirmation -->
    <div v-else class="space-y-4">
      <div class="bg-red-50 border border-red-200 rounded-lg p-4">
        <p class="font-medium text-red-900">Delete this list?</p>
        <p class="text-sm text-red-700 mt-1">
          This action cannot be undone. All tasks in this list will be permanently deleted.
        </p>
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
          Update
        </Button>
      </div>
      <div v-else class="flex gap-2 w-full">
        <Button
          type="button"
          variant="secondary"
          :disabled="isDeleting"
          @click="showDeleteConfirm = false"
        >
          Keep List
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
