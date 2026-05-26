<template>
  <div class="w-full">
    <label v-if="label" class="form-label">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    <div class="relative">
      <select
        :value="modelValue"
        :multiple="multiple"
        :disabled="disabled"
        :required="required"
        class="form-input appearance-none cursor-pointer pr-10 bg-white"
        :class="{ 'border-red-500 focus:ring-red-500': error }"
        @change="handleChange"
      >
        <option v-if="!multiple && placeholder" value="" disabled>{{ placeholder }}</option>
        <option v-for="opt in normalizedOptions" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
      <div
        class="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-500"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M19 14l-7 7m0 0l-7-7m7 7V3"
          />
        </svg>
      </div>
    </div>
    <p v-if="error" class="mt-1 text-sm text-red-600">
      {{ error }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Option {
  label: string;
  value: string | number;
}

const props = withDefaults(
  defineProps<{
    modelValue: string | number | (string | number)[];
    options: Option[] | (string | number)[];
    label?: string;
    placeholder?: string;
    error?: string | null;
    disabled?: boolean;
    required?: boolean;
    multiple?: boolean;
  }>(),
  {
    disabled: false,
    required: false,
    multiple: false,
  }
);

const emit = defineEmits<{
  'update:modelValue': [value: string | number | (string | number)[]];
  change: [value: string | number | (string | number)[]];
}>();

const normalizedOptions = computed(() => {
  return props.options.map((opt) => {
    if (typeof opt === 'object' && opt !== null) {
      return opt as Option;
    }
    return { label: String(opt), value: opt };
  });
});

const handleChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  let value: string | number | (string | number)[];

  if (props.multiple) {
    value = Array.from(target.selectedOptions).map((option) => {
      const numVal = Number(option.value);
      return Number.isNaN(numVal) ? option.value : numVal;
    });
  } else {
    const numVal = Number(target.value);
    value = Number.isNaN(numVal) ? target.value : numVal;
  }

  emit('update:modelValue', value);
  emit('change', value);
};
</script>
