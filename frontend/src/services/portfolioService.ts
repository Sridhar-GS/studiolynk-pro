import api from './api';
import {
  ApiResponse,
  CreateCategoryPayload,
  Portfolio,
  PortfolioCategory,
  PortfolioImage,
  UpdateCategoryPayload,
  UploadResponse,
} from '../types';

export const portfolioService = {
  async getMyPortfolio(): Promise<Portfolio> {
    const res = await api.get<ApiResponse<Portfolio>>('/portfolio/me');
    return res.data.data;
  },

  async getPortfolioByFreelancerId(freelancerId: number): Promise<Portfolio> {
    const res = await api.get<ApiResponse<Portfolio>>(`/portfolio/freelancer/${freelancerId}`);
    return res.data.data;
  },

  async createCategory(payload: CreateCategoryPayload): Promise<PortfolioCategory> {
    const res = await api.post<ApiResponse<PortfolioCategory>>('/portfolio/categories', payload);
    return res.data.data;
  },

  async updateCategory(categoryId: number, payload: UpdateCategoryPayload): Promise<PortfolioCategory> {
    const res = await api.put<ApiResponse<PortfolioCategory>>(`/portfolio/categories/${categoryId}`, payload);
    return res.data.data;
  },

  async deleteCategory(categoryId: number): Promise<void> {
    await api.delete(`/portfolio/categories/${categoryId}`);
  },

  async uploadImages(categoryId: number, files: File[]): Promise<PortfolioImage[]> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const res = await api.post<ApiResponse<PortfolioImage[]>>(
      `/portfolio/categories/${categoryId}/images`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return res.data.data;
  },

  async deleteImage(imageId: number): Promise<void> {
    await api.delete(`/portfolio/images/${imageId}`);
  },

  async reorderImages(categoryId: number, itemIds: number[]): Promise<PortfolioImage[]> {
    const res = await api.put<ApiResponse<PortfolioImage[]>>(
      `/portfolio/categories/${categoryId}/reorder`,
      { itemIds }
    );
    return res.data.data;
  },

  async reorderCategories(itemIds: number[]): Promise<PortfolioCategory[]> {
    const res = await api.put<ApiResponse<PortfolioCategory[]>>(
      '/portfolio/categories/reorder',
      { itemIds }
    );
    return res.data.data;
  },

  async uploadGeneralImage(file: File): Promise<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    const res = await api.post<ApiResponse<UploadResponse>>('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data.data;
  },
};
