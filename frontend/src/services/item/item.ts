import Api from "../../apis/api";
import { ItemRequest } from '../../interfaces/item/ItemRequest';
import { ItemResponse } from '../../interfaces/item/ItemResponse';

export const item = {
    async getAllItems(): Promise<ItemResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<ItemResponse[]>({ url: '/item' });
      return response.data;
    },
  
    async getItemById(id: number): Promise<ItemResponse> {
      const api = await Api.getInstance();
      const response = await api.get<ItemResponse>({ url: `/item/${id}` });
      return response.data;
    },  
  
    async createItem(data: FormData): Promise<ItemResponse> {
      const api = await Api.getInstance();
      const response = await api.post<FormData, ItemResponse>(data, { url: "/item" });
      return response.data;
  },
  
    
  
    async updateItem(id: number, data: ItemRequest): Promise<ItemResponse> {
      const api = await Api.getInstance();
      const response = await api.put<ItemRequest, ItemResponse>(data, { url: `/item/${id}` });
      return response.data;
    },
  
    async deleteItem(id: number): Promise<void> {
      const api = await Api.getInstance();
      await api.delete({ url: `/item/${id}` });
    },
  
    async approveItem(itemId: number, approve: boolean): Promise<ItemResponse> {
        const api = await Api.getInstance();
        const response = await api.post<void, ItemResponse>(
          undefined, // No hay cuerpo en la solicitud
          {
            url: `/item/${itemId}/approve`,
            params: { approve },
          }
        );
        return response.data;
      },
  
    async getItemsByCategory(categoryId: number): Promise<ItemResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<ItemResponse[]>({ url: `/item/category/${categoryId}` });
      return response.data;
    },
  
    async getItemsByUser(userId: number): Promise<ItemResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<ItemResponse[]>({ url: `/item/user/${userId}` });
      return response.data;
    },
  };