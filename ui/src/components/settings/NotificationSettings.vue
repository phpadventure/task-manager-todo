<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useSettingsStore } from '@/stores/settingsStore';
import { useNotifications } from '@/composables/useNotifications';
import Modal from '@/components/common/Modal.vue';
import Button from '@/components/common/Button.vue';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const settingsStore = useSettingsStore();
const { updateInterval } = useNotifications();

const intervalMs = ref(settingsStore.notificationInterval);
const presets = [
  { label: '10 seconds', value: 10000 },
  { label: '30 seconds', value: 30000 },
  { label: '1 minute', value: 60000 },
  { label: '5 minutes', value: 300000 },
  { label: '10 minutes', value: 600000 },
];

const displayInterval = computed(() => {
  const seconds = Math.round(intervalMs.value / 1000);
  if (seconds < 60) return `${seconds}s`;
  const minutes = Math.round(seconds / 60);
  return `${minutes}m`;
});

watch(
  () => settingsStore.notificationInterval,
  (newValue) => {
    intervalMs.value = newValue;
  }
);

const handlePreset = (value: number) => {
  intervalMs.value = value;
};

const handleSave = () => {
  updateInterval(intervalMs.value);
  emit('update:modelValue', false);
};

const handleClose = () => {
  emit('update:modelValue', false);
};
</script>

<template>
  <Modal :model-value="props.modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <template #header>
      <span>Notification Settings</span>
    </template>

    <div class="space-y-6">
      <!-- Interval Setting -->
      <div class="space-y-3">
        <label class="block text-sm font-medium text-gray-900"> Notification Check Interval </label>
        <p class="text-sm text-gray-600">
          How often the server checks for new overdue and due-soon tasks ({{ displayInterval }})
        </p>

        <!-- Preset Buttons -->
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="preset in presets"
            :key="preset.value"
            type="button"
            class="px-3 py-2 text-sm font-medium rounded-lg transition-colors"
            :class="
              intervalMs === preset.value
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            "
            @click="handlePreset(preset.value)"
          >
            {{ preset.label }}
          </button>
        </div>

        <!-- Custom Range Slider -->
        <div class="space-y-2">
          <label for="interval-slider" class="text-xs text-gray-600">Or set custom value:</label>
          <div class="flex items-center gap-3">
            <input
              id="interval-slider"
              v-model.number="intervalMs"
              type="range"
              min="5000"
              max="600000"
              step="5000"
              class="flex-1 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            />
            <span class="text-sm font-medium text-gray-900 w-12">{{ displayInterval }}</span>
          </div>
          <p class="text-xs text-gray-500">Range: 5 seconds to 10 minutes</p>
        </div>
      </div>

      <!-- Info Box -->
      <div class="bg-blue-50 border border-blue-200 rounded-lg p-3">
        <p class="text-sm text-blue-900">
          ℹ️ Shorter intervals get more frequent updates but use more resources. Longer intervals
          are more efficient but updates arrive less frequently.
        </p>
      </div>

      <!-- Action Buttons -->
      <div class="flex gap-3 justify-end pt-2">
        <Button variant="secondary" @click="handleClose"> Cancel </Button>
        <Button @click="handleSave"> Save Settings </Button>
      </div>
    </div>
  </Modal>
</template>
