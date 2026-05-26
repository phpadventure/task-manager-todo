import { onMounted, onUnmounted } from 'vue';
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
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const fullUrl = wsUrl.startsWith('ws') ? wsUrl : `${protocol}//${window.location.host}${wsUrl}`;

    notificationService = new NotificationService(fullUrl);

    try {
      await notificationService.connect();

      notificationService.onMessage((notification) => {
        if (!seenNotificationIds.has(notification.id)) {
          seenNotificationIds.add(notification.id);
          notificationStore.addNotification(notification);
        }
      });

      notificationService.onError((error) => {
        console.error('WebSocket error:', error);
      });

      notificationService.onClose(() => {
        console.log('WebSocket closed, will attempt reconnect');
      });

      settingsStore.loadSettings();
      notificationService.subscribe(settingsStore.notificationInterval);
    } catch (error) {
      console.error('Failed to connect WebSocket:', error);
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

  onMounted(() => {
    connect();
  });

  onUnmounted(() => {
    disconnect();
  });

  return {
    connect,
    disconnect,
    acknowledge,
    updateInterval,
  };
};
