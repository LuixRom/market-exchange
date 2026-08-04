import Api from "../../apis/api";
import { ReportRequest, ReportResponse, ReportReviewRequest, ReportStatus } from "../../interfaces/report/Report";

export const report = {
    async create(payload: ReportRequest): Promise<ReportResponse> {
        const api = await Api.getInstance();
        const response = await api.post<ReportRequest, ReportResponse>(payload, {
            url: "/reports",
        });
        return response.data;
    },

    async list(status?: ReportStatus): Promise<ReportResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<ReportResponse[]>({
            url: "/admin/reports",
            params: status ? { status } : undefined,
        });
        return response.data;
    },

    async get(id: number): Promise<ReportResponse> {
        const api = await Api.getInstance();
        const response = await api.get<ReportResponse>({
            url: `/admin/reports/${id}`,
        });
        return response.data;
    },

    async review(id: number, payload: ReportReviewRequest): Promise<ReportResponse> {
        const api = await Api.getInstance();
        const response = await api.put<ReportReviewRequest, ReportResponse>(payload, {
            url: `/admin/reports/${id}/review`,
        });
        return response.data;
    },
};
