import Api from "../../apis/api";
import { ChatMessageRequest, ChatMessageResponse } from "../../interfaces/chat/ChatMessage";

export const chat = {
    async listMessages(tradeProposalId: number): Promise<ChatMessageResponse[]> {
        const api = await Api.getInstance();
        const response = await api.get<ChatMessageResponse[]>({
            url: `/agreements/${tradeProposalId}/messages`,
        });
        return response.data;
    },

    async sendMessage(tradeProposalId: number, content: string): Promise<ChatMessageResponse> {
        const api = await Api.getInstance();
        const payload: ChatMessageRequest = { content };
        const response = await api.post<ChatMessageRequest, ChatMessageResponse>(payload, {
            url: `/agreements/${tradeProposalId}/messages`,
        });
        return response.data;
    },
};
