import Api from '../../apis/api';
import { CategoryRequest } from '../../interfaces/category/CategoryRequest';
import { CategoryResponse } from '../../interfaces/category/CategoryResponse';

export const category = {
    async getAllCategories(): Promise<CategoryResponse[]> {
      const api = await Api.getInstance();
      const response = await api.get<CategoryResponse[]>({ url: '/category' });
      return response.data;
    },
  
    async getCategoryById(id: number): Promise<CategoryResponse> {
      const api = await Api.getInstance();
      const response = await api.get<CategoryResponse>({ url: `/category/${id}` });
      return response.data;
    },
  
    async createCategory(data: CategoryRequest): Promise<CategoryResponse> {
      const api = await Api.getInstance();
      const response = await api.post<CategoryRequest, CategoryResponse>(data, {
        url: '/category',
      });
      return response.data;
    },
  
    async updateCategory(id: number, data: CategoryRequest): Promise<CategoryResponse> {
      const api = await Api.getInstance();
      const response = await api.put<CategoryRequest, CategoryResponse>(data, {
        url: `/category/${id}`,
      });
      return response.data;
    },
  
    async deleteCategory(id: number): Promise<void> {
      const api = await Api.getInstance();
      await api.delete({ url: `/category/${id}` });
    },
};