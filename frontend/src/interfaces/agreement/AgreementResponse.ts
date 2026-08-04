export type AgreementStatus = "PENDING" | "ACCEPTED" | "REJECTED" | "CANCELLED" | "EXPIRED" | "COMPLETED";

export interface AgreementResponse {
    id: number;
    status: AgreementStatus;
    offeredItemId: number;
    offeredItemName: string;
    requestedItemId: number;
    requestedItemName: string;
    proposerId: number;
    proposerEmail: string;
    receiverId: number;
    receiverEmail: string;
    initialMessage?: string;
    createdAt?: string;
    updatedAt?: string;
    cancelledAt?: string;
}
