import { Client, StompSubscription } from '@stomp/stompjs';

interface WebSocketNotification {
  type: 'notification';
  id: string;
  taskId: string;
  title: string;
  reason: 'overdue' | 'due-soon';
  dueDate: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  sentAt: string;
}

interface SubscribeMessage {
  interval: number;
}

interface AckMessage {
  type: 'ack';
  notificationId: string;
}

type MessageHandler = (data: WebSocketNotification) => void;
type ErrorHandler = (error: string) => void;
type CloseHandler = () => void;

export class NotificationService {
  private client: Client | null = null;
  private url: string;
  private isConnecting = false;

  private messageHandlers: MessageHandler[] = [];
  private errorHandlers: ErrorHandler[] = [];
  private closeHandlers: CloseHandler[] = [];
  private subscriptions: StompSubscription[] = [];

  constructor(url: string) {
    this.url = url;
  }

  connect(): Promise<void> {
    if (this.client?.connected) {
      return Promise.resolve();
    }
    if (this.isConnecting) {
      return Promise.resolve(); // Запобігаємо подвійному підключенню у Vue Strict Mode
    }

    this.isConnecting = true;

    return new Promise((resolve, reject) => {
      try {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = this.url.startsWith('ws')
          ? this.url
          : `${protocol}//${window.location.host}${this.url}`;

        console.log('[WS] Creating STOMP client with URL:', wsUrl);

        this.client = new Client({
          brokerURL: wsUrl,
          // Дозволяємо бібліотеці самій керувати реконнектом (кожні 5 секунд)
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          debug: (msg: string) => {
            if (msg.includes('CONNECT') || msg.includes('SUBSCRIBE') || msg.includes('MESSAGE')) {
              console.log('[STOMP DEBUG]', msg);
            }
          },
        });

        this.client.onConnect = (frame) => {
          console.log('[WS] STOMP Connected:', frame.command);
          this.isConnecting = false;
          this.subscriptions = [];

          // 1. ПІДПИСКА НА ПОЧАТКОВИЙ ПАКЕТ (/app/notifications)
          console.log('[WS] Subscribing to /app/notifications...');
          const appNotifSub = this.client?.subscribe('/app/notifications', (message) => {
            try {
              const parsed = JSON.parse(message.body);
              console.log('[WS] Received initial batch:', parsed);
              const notifications = Array.isArray(parsed) ? parsed : [parsed];
              notifications.forEach((data) => {
                this.messageHandlers.forEach((handler) => handler(data));
              });
            } catch (error) {
              console.error('[WS] Error parsing initial batch:', error);
            }
          });
          if (appNotifSub) this.subscriptions.push(appNotifSub);

          // 2. МИТТЄВА ПІДПИСКА НА ДИНАМІЧНУ ЧЕРГУ КОРИСТУВАЧА
          // ПРИБРАНО setTimeout(..., 1000) щоб уникнути втрати повідомлень від шедулера!
          console.log('[WS] Subscribing to /user/queue/notifications...');
          const userQueueSub = this.client?.subscribe('/user/queue/notifications', (message) => {
            try {
              const data = JSON.parse(message.body);
              console.log('[WS] Received notification on /user/queue:', data.title);
              this.messageHandlers.forEach((handler) => handler(data));
            } catch (error) {
              console.error('[WS] Error parsing /user/queue notification:', error);
            }
          });

          if (userQueueSub) {
            this.subscriptions.push(userQueueSub);
            console.log('[WS] ✅ /user/queue/notifications subscribed');
          }

          resolve();
        };

        this.client.onStompError = (frame) => {
          this.isConnecting = false;
          console.error('[WS] STOMP error:', frame.body);
          const errorMsg = `STOMP Error: ${frame.body}`;
          this.errorHandlers.forEach((handler) => handler(errorMsg));
          reject(new Error(errorMsg));
        };

        this.client.onDisconnect = () => {
          this.isConnecting = false;
          console.log('[WS] STOMP deactivated/disconnected');
          this.closeHandlers.forEach((handler) => handler());
        };

        this.client.onWebSocketClose = () => {
          console.log('[WS] Underlying WebSocket closed. Library will auto-reconnect...');
        };

        console.log('[WS] Activating STOMP connection...');
        this.client.activate();
      } catch (error) {
        this.isConnecting = false;
        console.error('[WS] Failed to create STOMP client:', error);
        reject(error);
      }
    });
  }

  disconnect(): void {
    this.isConnecting = false;
    this.subscriptions.forEach((sub) => {
      if (sub?.unsubscribe) {
        sub.unsubscribe();
      }
    });
    this.subscriptions = [];

    if (this.client) {
      console.log('[WS] Deactivating STOMP client...');
      this.client.deactivate();
      this.client = null;
    }
  }

  subscribe(interval: number): void {
    if (this.client?.connected) {
      const message: SubscribeMessage = { interval };
      console.log('[WS] Sending subscribe message with interval:', interval);
      this.client.publish({
        destination: '/app/subscribe',
        body: JSON.stringify(message),
      });
    } else {
      console.warn('[WS] Client not connected, cannot subscribe');
    }
  }

  acknowledge(notificationId: string): void {
    if (this.client?.connected) {
      const message: AckMessage = { type: 'ack', notificationId };
      console.log('[WS] Sending acknowledge message for:', notificationId);
      this.client.publish({
        destination: '/app/ack',
        body: JSON.stringify(message),
      });
    } else {
      console.warn('[WS] Client not connected, cannot acknowledge');
    }
  }

  onMessage(handler: MessageHandler): void {
    this.messageHandlers.push(handler);
  }

  onError(handler: ErrorHandler): void {
    this.errorHandlers.push(handler);
  }

  onClose(handler: CloseHandler): void {
    this.closeHandlers.push(handler);
  }

  isConnected(): boolean {
    return this.client !== null && this.client.connected;
  }
}
