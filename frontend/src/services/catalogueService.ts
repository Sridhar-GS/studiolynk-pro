import api from './api';
import { ApiResponse, EquipmentCategory, EquipmentItem, ServiceItem, Skill } from '../types';

export const catalogueService = {
  async getSkills(): Promise<Skill[]> {
    const res = await api.get<ApiResponse<Skill[]>>('/skills');
    return res.data.data;
  },

  async createCustomSkill(name: string): Promise<Skill> {
    const res = await api.post<ApiResponse<Skill>>('/skills/custom', { name });
    return res.data.data;
  },

  async getServices(): Promise<ServiceItem[]> {
    const res = await api.get<ApiResponse<ServiceItem[]>>('/services');
    return res.data.data;
  },

  async createCustomService(name: string): Promise<ServiceItem> {
    const res = await api.post<ApiResponse<ServiceItem>>('/services/custom', { name });
    return res.data.data;
  },

  async getEquipmentCategories(): Promise<EquipmentCategory[]> {
    const res = await api.get<ApiResponse<EquipmentCategory[]>>('/equipment/categories');
    return res.data.data;
  },

  async getAllEquipment(): Promise<EquipmentItem[]> {
    const res = await api.get<ApiResponse<EquipmentItem[]>>('/equipment');
    return res.data.data;
  },

  async getEquipmentByCategory(categoryId: number): Promise<EquipmentItem[]> {
    const res = await api.get<ApiResponse<EquipmentItem[]>>(`/equipment/category/${categoryId}`);
    return res.data.data;
  },

  async createCustomEquipment(categoryId: number, name: string): Promise<EquipmentItem> {
    const res = await api.post<ApiResponse<EquipmentItem>>('/equipment/custom', { categoryId, name });
    return res.data.data;
  },
};
