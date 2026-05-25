import { ref, watch } from 'vue';

export const useLocalStorage = <T>(key: string, initialValue: T) => {
  const data = ref<T>(initialValue);

  const load = () => {
    try {
      const item = window.localStorage.getItem(key);
      if (item) {
        data.value = JSON.parse(item) as T;
      }
    } catch (err) {
      console.error(`Failed to load from localStorage:`, err);
    }
  };

  const save = () => {
    try {
      window.localStorage.setItem(key, JSON.stringify(data.value));
    } catch (err) {
      console.error(`Failed to save to localStorage:`, err);
    }
  };

  const remove = () => {
    try {
      window.localStorage.removeItem(key);
      data.value = initialValue;
    } catch (err) {
      console.error(`Failed to remove from localStorage:`, err);
    }
  };

  watch(data, save, { deep: true });

  load();

  return {
    data,
    load,
    save,
    remove,
  };
};
