export interface ChatMessageRequest {
    content: string;
}

export interface ChatMessageResponse {
    id: number;
    tradeProposalId: number;
    senderId: number;
    senderEmail: string;
    content: string;
    createdAt?: string;
    readAt?: string;
}
