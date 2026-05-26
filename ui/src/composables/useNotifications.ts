import { useNotificationStore } from '@/stores/notificationStore';
import { useSettingsStore } from '@/stores/settingsStore';
import { NotificationService } from '@/websocket/notificationService';

let notificationService: NotificationService | null = null;
const seenNotificationIds = new Set<string>();

export const useNotifications = () => {
  const notificationStore = useNotificationStore();
  const settingsStore = useSettingsStore();

  const connect = async () => {
    const wsUrl = import.meta.env.VITE_WS_URL || '/api/ws';
    console.log('[WS] Connecting to:', wsUrl);
    notificationService = new NotificationService(wsUrl);

    try {
      await notificationService.connect();
      console.log('[WS] STOMP connection established');

      notificationService.onMessage((notification) => {
        console.log('[WS] Received notification:', notification);
        console.log('[WS] Title:', notification.title, 'DueDate:', notification.dueDate);
        if (!seenNotificationIds.has(notification.id)) {
          seenNotificationIds.add(notification.id);
          notificationStore.addNotification(notification);
          console.log('[WS] Notification added to store:', {
            id: notification.id,
            title: notification.title,
            dueDate: notification.dueDate,
            reason: notification.reason,
            priority: notification.priority,
          });
        } else {
          console.log('[WS] Duplicate notification ignored:', notification.id);
        }
      });

      notificationService.onError((error) => {
        console.error('[WS] Error:', error);
      });

      notificationService.onClose(() => {
        console.log('[WS] Connection closed, will attempt reconnect');
      });

      settingsStore.loadSettings();
    } catch (error) {
      console.error('[WS] Failed to connect:', error);
    }
  };

  const disconnect = () => {
    if (notificationService) {
      notificationService.disconnect();
      notificationService = null;
    }
  };

  const acknowledge = (notificationId: string) => {
    if (notificationService) {
      notificationService.acknowledge(notificationId);
    }
    notificationStore.acknowledgeNotification(notificationId);
  };

  const updateInterval = (newInterval: number) => {
    settingsStore.setNotificationInterval(newInterval);
    if (notificationService && notificationService.isConnected()) {
      notificationService.subscribe(newInterval);
    }
  };

  return {
    connect,
    disconnect,
    acknowledge,
    updateInterval,
  };
};
