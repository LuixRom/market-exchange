import Api from "../../apis/api"; // Importa la configuración de tu cliente Axios
import { AgreementRequest } from "../../interfaces/agreement/AgreementRequest";
import { AgreementResponse } from "../../interfaces/agreement/AgreementResponse";

export const Agreement = {
    /**
     * Obtener todos los acuerdos
     * @returns Lista de acuerdos
     */
    async getAllAgreements(): Promise<AgreementResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<AgreementResponse[]>({ url: "/agreements" });
        return response.data
    },

    async getSentAgreements(): Promise<AgreementResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<AgreementResponse[]>({ url: "/agreements/sent" });
        return response.data;
    },

    async getReceivedAgreements(): Promise<AgreementResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<AgreementResponse[]>({ url: "/agreements/received" });
        return response.data;
    },

    async getMyAgreements(): Promise<AgreementResponse[]> {
        const [sent, received] = await Promise.all([
            this.getSentAgreements(),
            this.getReceivedAgreements(),
        ]);
        return [...sent, ...received].sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return dateB - dateA;
        });
    },

    /**
     * Crear un nuevo acuerdo
     * @param agreementRequest Solicitud para crear un acuerdo
     * @returns Acuerdo creado
     */
    async createAgreement(agreementRequest: AgreementRequest): Promise<AgreementResponse> {
        const api = await Api.getInstance();
        const response = await api.post<AgreementRequest, AgreementResponse>(agreementRequest, {
            url: "/agreements",
        });
        return response.data
    },

    /**
     * Obtener un acuerdo por ID
     * @param id ID del acuerdo
     * @returns Detalles del acuerdo
     */
    async getAgreementById(id: number): Promise<AgreementResponse> {
        const api = await Api.getInstance();
        const response = await api.get<AgreementResponse>({ url: `/agreements/${id}` });
        return response.data
    },

    /**
     * Actualizar un acuerdo
     * @param id ID del acuerdo
     * @param agreementRequest Solicitud de actualización
     * @returns Acuerdo actualizado
     */
    async updateAgreement(id: number, agreementRequest: AgreementRequest): Promise<AgreementResponse> {
        const api = await Api.getInstance();
        const response = await api.put<AgreementRequest, AgreementResponse>(agreementRequest, {
            url: `/agreements/${id}`,
        });
        return response.data
    },

    /**
     * Aceptar un acuerdo
     * @param id ID del acuerdo
     * @returns Acuerdo aceptado
     */
    async acceptAgreement(id: number): Promise<AgreementResponse> {
        const api = await Api.getInstance();
        const response = await api.put<void, AgreementResponse>(undefined, { url: `/agreements/${id}/accept` });
        return response.data;
    },
    
    async rejectAgreement(id: number): Promise<AgreementResponse> {
        const api = await Api.getInstance();
        const response = await api.put<void, AgreementResponse>(undefined, { url: `/agreements/${id}/reject` });
        return response.data;
    },

    async cancelAgreement(id: number): Promise<AgreementResponse> {
        const api = await Api.getInstance();
        const response = await api.put<void, AgreementResponse>(undefined, { url: `/agreements/${id}/cancel` });
        return response.data;
    },

    /**
     * Eliminar un acuerdo
     * @param id ID del acuerdo
     */
    async deleteAgreement(id: number): Promise<void> {
        const api = await Api.getInstance();
        await api.delete({ url: `/agreements/${id}` });
    },
};
