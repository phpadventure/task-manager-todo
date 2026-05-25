<template>
  <div class="w-full">
    <label v-if="label" class="form-label">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      class="form-input"
      :class="{ 'border-red-500 focus:ring-red-500': error }"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      @blur="$emit('blur')"
    />
    <p v-if="error" class="mt-1 text-sm text-red-600">
      {{ error }}
    </p>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: string | number;
    type?: string;
    label?: string;
    placeholder?: string;
    error?: string | null;
    disabled?: boolean;
    required?: boolean;
  }>(),
  {
    type: 'text',
    disabled: false,
    required: false,
  }
);

defineEmits<{
  'update:modelValue': [value: string];
  blur: [];
}>();
</script>
