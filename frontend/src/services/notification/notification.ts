import Api from "../../apis/api";
import { NotificationResponse, UnreadCountResponse } from "../../interfaces/notification/NotificationResponse";

export const notification = {
    async list(unreadOnly = false): Promise<NotificationResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<NotificationResponse[]>({
            url: `/notifications?unreadOnly=${unreadOnly}`,
        });
        return response.data;
    },

    async unreadCount(): Promise<number> {
        const api = await Api.getInstance();
        const response = await api.get<UnreadCountResponse>({
            url: "/notifications/unread-count",
        });
        return response.data.count;
    },

    async markAsRead(id: number): Promise<NotificationResponse> {
        const api = await Api.getInstance();
        const response = await api.put<Record<string, never>, NotificationResponse>({}, {
            url: `/notifications/${id}/read`,
        });
        return response.data;
    },
};
