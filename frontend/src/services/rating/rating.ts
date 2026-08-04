import Api from "../../apis/api";
import { RatingReputation, RatingRequest, RatingResponse } from "../../interfaces/rating/Rating";

export const rating = {
    async create(payload: RatingRequest): Promise<RatingResponse> {
        const api = await Api.getInstance();
        const response = await api.post<RatingRequest, RatingResponse>(payload, {
            url: "/ratings/crear",
        });
        return response.data;
    },

    async update(id: number, payload: RatingRequest): Promise<RatingResponse> {
        const api = await Api.getInstance();
        const response = await api.put<RatingRequest, RatingResponse>(payload, {
            url: `/ratings/${id}`,
        });
        return response.data;
    },

    async listByUser(userId: number): Promise<RatingResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<RatingResponse[]>({
            url: `/ratings/usuario/${userId}`,
        });
        return response.data;
    },

    async getReputation(userId: number): Promise<RatingReputation> {
        const api = await Api.getInstance();
        const response = await api.get<RatingReputation>({
            url: `/ratings/usuario/${userId}/reputation`,
        });
        return response.data;
    },
};
