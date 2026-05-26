import { Client } from '@stomp/stompjs';

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
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  private messageHandlers: MessageHandler[] = [];
  private errorHandlers: ErrorHandler[] = [];
  private closeHandlers: CloseHandler[] = [];

  constructor(url: string) {
    this.url = url;
  }

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = this.url.startsWith('ws')
          ? this.url
          : `${protocol}//${window.location.host}${this.url}`;
        console.log('[WS] Creating STOMP client with raw WebSocket...');

        this.client = new Client({
          brokerURL: wsUrl,
          reconnectDelay: this.reconnectDelay * this.reconnectAttempts,
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
          this.reconnectAttempts = 0;

          // Step 1: Subscribe to /app/notifications for initial batch
          console.log('[WS] Subscribing to /app/notifications (initial batch)...');
          this.client?.subscribe('/app/notifications', (message) => {
            try {
              const parsed = JSON.parse(message.body);
              console.log('[WS] Received initial batch:', parsed);
              // Handle both single notification and array of notifications
              const notifications = Array.isArray(parsed) ? parsed : [parsed];
              notifications.forEach((data) => {
                console.log('[WS] Processing notification:', data);
                this.messageHandlers.forEach((handler) => handler(data));
              });
            } catch (error) {
              console.error(
                '[WS] Failed to parse initial notifications:',
                error,
                'Body:',
                message.body
              );
            }
          });

          // Step 2: Subscribe to /user/queue/notifications for ongoing messages
          console.log('[WS] Subscribing to /user/queue/notifications (ongoing messages)...');
          this.client?.subscribe('/user/queue/notifications', (message) => {
            try {
              const data = JSON.parse(message.body) as WebSocketNotification;
              console.log('[WS] Received ongoing notification:', data.title);
              this.messageHandlers.forEach((handler) => handler(data));
            } catch (error) {
              console.error('[WS] Failed to parse ongoing notification:', error);
            }
          });

          resolve();
        };

        this.client.onStompError = (frame) => {
          console.error('[WS] STOMP error:', frame.body);
          const errorMsg = `STOMP Error: ${frame.body}`;
          this.errorHandlers.forEach((handler) => handler(errorMsg));
          reject(new Error(errorMsg));
        };

        this.client.onDisconnect = (frame) => {
          console.log('[WS] STOMP disconnected');
          this.closeHandlers.forEach((handler) => handler());
          this.attemptReconnect();
        };

        console.log('[WS] Activating STOMP connection to:', wsUrl);
        this.client.activate();
      } catch (error) {
        console.error('[WS] Failed to create STOMP client:', error);
        reject(error);
      }
    });
  }

  disconnect(): void {
    if (this.client && this.client.connected) {
      console.log('[WS] Disconnecting STOMP client...');
      this.client.deactivate();
      this.client = null;
    }
  }

  subscribe(interval: number): void {
    if (this.client && this.client.connected) {
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
    if (this.client && this.client.connected) {
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

  private attemptReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = this.reconnectDelay * this.reconnectAttempts;
      console.log(
        `[WS] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})...`
      );
      setTimeout(() => {
        this.connect().catch((error) => {
          console.error('[WS] Reconnection failed:', error);
        });
      }, delay);
    } else {
      console.error('[WS] Max reconnection attempts reached');
    }
  }

  isConnected(): boolean {
    return this.client !== null && this.client.connected;
  }
}
