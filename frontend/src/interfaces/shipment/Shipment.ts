export type ShipmentStatus = "PENDING" | "PREPARING" | "IN_TRANSIT" | "DELIVERED" | "CANCELLED";

export type ShipmentMethod = "EXTERNAL_SHIPPING" | "IN_PERSON";

export interface ShipmentResponse {
    id: number;
    initiatorAddress: string;
    receiveAddress: string;
    deliveryDate?: string | null;
    tradeProposalId: number;
    status: ShipmentStatus;
    method: ShipmentMethod;
    trackingCode?: string | null;
    createdAt?: string;
    updatedAt?: string | null;
    preparedAt?: string | null;
    shippedAt?: string | null;
    deliveredAt?: string | null;
    cancelledAt?: string | null;
    proposerDeliveryConfirmedAt?: string | null;
    receiverDeliveryConfirmedAt?: string | null;
    deliveryConfirmedByBoth: boolean;
}

export interface ShipmentUpdateRequest {
    initiatorAddress?: string;
    receiveAddress?: string;
    deliveryDate?: string;
    method?: ShipmentMethod;
}

export interface ShipmentShipRequest {
    trackingCode?: string;
}
