export interface NotificationResponse {
    id: number;
    type: string;
    title: string;
    message: string;
    tradeProposalId?: number | null;
    itemId?: number | null;
    read: boolean;
    createdAt?: string;
    readAt?: string | null;
}

export interface UnreadCountResponse {
    count: number;
}
