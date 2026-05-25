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
type ErrorHandler = (error: Event) => void;
type CloseHandler = () => void;

export class NotificationService {
  private ws: WebSocket | null = null;
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
        this.ws = new WebSocket(this.url);

        this.ws.onopen = () => {
          this.reconnectAttempts = 0;
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data) as WebSocketNotification;
            this.messageHandlers.forEach((handler) => handler(data));
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        };

        this.ws.onerror = (error) => {
          this.errorHandlers.forEach((handler) => handler(error));
          reject(error);
        };

        this.ws.onclose = () => {
          this.closeHandlers.forEach((handler) => handler());
          this.attemptReconnect();
        };
      } catch (error) {
        reject(error);
      }
    });
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  subscribe(interval: number): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message: SubscribeMessage = { interval };
      this.ws.send(JSON.stringify(message));
    }
  }

  acknowledge(notificationId: string): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message: AckMessage = { type: 'ack', notificationId };
      this.ws.send(JSON.stringify(message));
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
      setTimeout(() => {
        this.connect().catch((error) => {
          console.error('Reconnection failed:', error);
        });
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}
