import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useSettingsStore = defineStore('settings', () => {
  const notificationInterval = ref<number>(60000); // Default 60 seconds in milliseconds

  const setNotificationInterval = (interval: number) => {
    if (interval > 0) {
      notificationInterval.value = interval;
      localStorage.setItem('notificationInterval', interval.toString());
    }
  };

  const loadSettings = () => {
    const savedInterval = localStorage.getItem('notificationInterval');
    if (savedInterval) {
      notificationInterval.value = parseInt(savedInterval, 10);
    }
  };

  return {
    notificationInterval,
    setNotificationInterval,
    loadSettings,
  };
});
