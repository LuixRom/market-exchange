import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getApiBaseUrl } from "../../apis/api";
import { ChatMessageResponse } from "../../interfaces/chat/ChatMessage";
import { AgreementResponse } from "../../interfaces/agreement/AgreementResponse";
import { NotificationResponse } from "../../interfaces/notification/NotificationResponse";

type MessageCallback = (message: ChatMessageResponse) => void;
type NotificationCallback = (notification: NotificationResponse) => void;
type AgreementEventCallback = (agreement: AgreementResponse) => void;
type ConnectionCallback = (connected: boolean) => void;

class RealtimeClient {
    private client: Client | null = null;
    private activationPromise: Promise<void> | null = null;

    connect(onConnectionChange?: ConnectionCallback): Promise<void> {
        const token = localStorage.getItem("accessToken");

        if (!token) {
            return Promise.reject(new Error("No hay token para conectar al WebSocket."));
        }

        if (this.client?.connected) {
            onConnectionChange?.(true);
            return Promise.resolve();
        }

        if (this.activationPromise) {
            return this.activationPromise.then(() => {
                onConnectionChange?.(this.client?.connected ?? false);
            });
        }

        this.client = new Client({
            webSocketFactory: () => new SockJS(`${getApiBaseUrl()}/ws`),
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: () => undefined,
            onConnect: () => {
                onConnectionChange?.(true);
            },
            onWebSocketClose: () => {
                onConnectionChange?.(false);
            },
            onStompError: () => {
                onConnectionChange?.(false);
            },
        });

        this.activationPromise = new Promise((resolve, reject) => {
            if (!this.client) {
                reject(new Error("No se pudo crear el cliente WebSocket."));
                return;
            }

            const timeoutId = window.setTimeout(() => {
                this.activationPromise = null;
                reject(new Error("Timeout conectando al WebSocket."));
            }, 8000);

            this.client.onConnect = () => {
                window.clearTimeout(timeoutId);
                this.activationPromise = null;
                onConnectionChange?.(true);
                resolve();
            };

            this.client.onStompError = () => {
                window.clearTimeout(timeoutId);
                this.activationPromise = null;
                onConnectionChange?.(false);
                reject(new Error("Error STOMP al conectar."));
            };

            this.client.activate();
        });

        return this.activationPromise;
    }

    subscribeToAgreementMessages(tradeProposalId: number, callback: MessageCallback): StompSubscription | null {
        if (!this.client?.connected) {
            return null;
        }

        const subscriptions = [
            this.client.subscribe("/user/queue/agreement-messages", (payload: IMessage) => {
                const message = JSON.parse(payload.body) as ChatMessageResponse;
                if (message.tradeProposalId === tradeProposalId) {
                    callback(message);
                }
            }),
            this.client.subscribe(`/topic/agreements/${tradeProposalId}/messages`, (payload: IMessage) => {
                const message = JSON.parse(payload.body) as ChatMessageResponse;
                callback(message);
            }),
        ];

        return {
            id: subscriptions.map((subscription) => subscription.id).join(","),
            unsubscribe: () => {
                subscriptions.forEach((subscription) => subscription.unsubscribe());
            },
        };
    }

    sendAgreementMessage(tradeProposalId: number, content: string): boolean {
        if (!this.client?.connected) {
            return false;
        }

        this.client.publish({
            destination: `/app/agreements/${tradeProposalId}/messages`,
            body: JSON.stringify({ content }),
        });
        return true;
    }

    subscribeToNotifications(callback: NotificationCallback): StompSubscription | null {
        if (!this.client?.connected) {
            return null;
        }

        return this.client.subscribe("/user/queue/notifications", (payload: IMessage) => {
            callback(JSON.parse(payload.body) as NotificationResponse);
        });
    }

    subscribeToAgreementEvents(callback: AgreementEventCallback): StompSubscription | null {
        if (!this.client?.connected) {
            return null;
        }

        return this.client.subscribe("/user/queue/agreement-events", (payload: IMessage) => {
            callback(JSON.parse(payload.body) as AgreementResponse);
        });
    }

    disconnect() {
        this.client?.deactivate();
        this.client = null;
        this.activationPromise = null;
    }
}

export const realtimeClient = new RealtimeClient();
