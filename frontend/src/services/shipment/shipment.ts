import Api from "../../apis/api";
import { ShipmentResponse, ShipmentShipRequest, ShipmentUpdateRequest } from "../../interfaces/shipment/Shipment";

export const shipment = {
    async getByTradeProposalId(tradeProposalId: number): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.get<ShipmentResponse>({
            url: `/shipments/trade-proposal/${tradeProposalId}`,
        });
        return response.data;
    },

    async update(id: number, payload: ShipmentUpdateRequest): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.put<ShipmentUpdateRequest, ShipmentResponse>(payload, {
            url: `/shipments/${id}`,
        });
        return response.data;
    },

    async prepare(id: number): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, ShipmentResponse>({}, {
            url: `/shipments/${id}/prepare`,
        });
        return response.data;
    },

    async ship(id: number, payload: ShipmentShipRequest): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.put<ShipmentShipRequest, ShipmentResponse>(payload, {
            url: `/shipments/${id}/ship`,
        });
        return response.data;
    },

    async deliver(id: number): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, ShipmentResponse>({}, {
            url: `/shipments/${id}/deliver`,
        });
        return response.data;
    },

    async confirmDelivery(id: number): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, ShipmentResponse>({}, {
            url: `/shipments/${id}/confirm-delivery`,
        });
        return response.data;
    },

    async cancel(id: number): Promise<ShipmentResponse> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, ShipmentResponse>({}, {
            url: `/shipments/${id}/cancel`,
        });
        return response.data;
    },
};
