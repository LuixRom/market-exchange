import Api from "../../apis/api";
import { ItemRequest } from '../../interfaces/item/ItemRequest';
import { ItemResponse } from '../../interfaces/item/ItemResponse';
import { ItemImageResponse } from "../../interfaces/item/ItemImageResponse";
import { PageResponse } from "../../interfaces/PageResponse";

export type CatalogParams = {
  categoryId?: number;
  userId?: number;
  condition?: string;
  status?: string;
  q?: string;
  page?: number;
  size?: number;
  sort?: string;
};

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

    async getCatalog(params: CatalogParams = {}): Promise<PageResponse<ItemResponse>> {
      const api = await Api.getInstance();
      const response = await api.get<PageResponse<ItemResponse>>({ url: '/item/catalog', params });
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

    async getMyItems(): Promise<ItemResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<ItemResponse[]>({ url: '/item/mine' });
      return response.data;
    },
  
    async approveItem(itemId: number, approve: boolean, reason?: string): Promise<ItemResponse> {
        const api = await Api.getInstance();
        const response = await api.post<void, ItemResponse>(
          undefined, // No hay cuerpo en la solicitud
          {
            url: `/item/${itemId}/approve`,
            params: { approve, ...(reason ? { reason } : {}) },
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

    async getImages(itemId: number): Promise<ItemImageResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<ItemImageResponse[]>({ url: `/item/${itemId}/images` });
      return response.data;
    },

    async addImage(itemId: number, image: File): Promise<ItemImageResponse> {
      const api = await Api.getInstance();
      const formData = new FormData();
      formData.append("image", image);
      const response = await api.post<FormData, ItemImageResponse>(formData, { url: `/item/${itemId}/images` });
      return response.data;
    },

    async deleteImage(itemId: number, imageId: number): Promise<void> {
      const api = await Api.getInstance();
      await api.delete({ url: `/item/${itemId}/images/${imageId}` });
    },

    async markPrimaryImage(itemId: number, imageId: number): Promise<void> {
      const api = await Api.getInstance();
      await api.put<void, void>(undefined, { url: `/item/${itemId}/images/${imageId}/primary` });
    },

    async addFavorite(itemId: number): Promise<void> {
      const api = await Api.getInstance();
      await api.post<void, void>(undefined, { url: `/item/${itemId}/favorite` });
    },

    async removeFavorite(itemId: number): Promise<void> {
      const api = await Api.getInstance();
      await api.delete({ url: `/item/${itemId}/favorite` });
    },

    async getFavorites(): Promise<ItemResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<ItemResponse[]>({ url: '/item/favorites' });
      return response.data;
    },
  };
