import { defineStore } from 'pinia';
import { ref } from 'vue';

interface TaskNotification {
  id: string;
  type: 'task';
  taskId: string;
  title: string;
  reason: 'overdue' | 'due-soon';
  dueDate: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  sentAt: string;
}

interface ErrorNotification {
  id: string;
  type: 'error';
  message: string;
  duration?: number;
}

type Notification = TaskNotification | ErrorNotification;

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([]);

  const addNotification = (notification: Omit<TaskNotification, 'type'>) => {
    const taskNotif: TaskNotification = {
      type: 'task',
      ...notification,
    } as TaskNotification;
    notifications.value.push(taskNotif);
  };

  const addErrorNotification = (notification: Omit<ErrorNotification, 'id' | 'type'>) => {
    const errorNotif: ErrorNotification = {
      id: `error-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      type: 'error',
      ...notification,
    };
    notifications.value.push(errorNotif);

    // Auto-dismiss after duration (default 5 seconds)
    const duration = notification.duration || 5000;
    setTimeout(() => {
      removeNotification(errorNotif.id);
    }, duration);
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
    addErrorNotification,
    removeNotification,
    acknowledgeNotification,
  };
});
