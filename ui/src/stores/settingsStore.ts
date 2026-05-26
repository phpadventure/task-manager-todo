import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useSettingsStore = defineStore('settings', () => {
  const notificationInterval = ref<number>(60000); // Default 60 seconds in milliseconds
  const toastDismissTime = ref<number>(30000); // Default 30 seconds in milliseconds

  const setNotificationInterval = (interval: number) => {
    if (interval > 0) {
      notificationInterval.value = interval;
      localStorage.setItem('notificationInterval', interval.toString());
    }
  };

  const setToastDismissTime = (time: number) => {
    if (time > 0) {
      toastDismissTime.value = time;
      localStorage.setItem('toastDismissTime', time.toString());
    }
  };

  const loadSettings = () => {
    const savedInterval = localStorage.getItem('notificationInterval');
    if (savedInterval) {
      notificationInterval.value = parseInt(savedInterval, 10);
    }
    const savedDismissTime = localStorage.getItem('toastDismissTime');
    if (savedDismissTime) {
      toastDismissTime.value = parseInt(savedDismissTime, 10);
    }
  };

  return {
    notificationInterval,
    toastDismissTime,
    setNotificationInterval,
    setToastDismissTime,
    loadSettings,
  };
});
