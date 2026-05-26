import { defineStore } from 'pinia';
import { ref } from 'vue';

interface Notification {
  id: string;
  taskId: string;
  title: string;
  reason: 'overdue' | 'due-soon';
  dueDate: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  sentAt: string;
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([]);

  const addNotification = (notification: Notification) => {
    notifications.value.push(notification);
  };

  const removeNotification = (id: string) => {
    notifications.value = notifications.value.filter((n) => n.id !== id);
  };

  const acknowledgeNotification = (id: string) => {
    removeNotification(id);
  };

  return {
    notifications,
    addNotification,
    removeNotification,
    acknowledgeNotification,
  };
});
