import axios from 'axios';
import {
  ApiResponse,
  AdminDashboardMetrics,
  AdminStudioItem,
  AdminFreelancerItem,
  AdminRequirementItem,
  AdminRequestItem,
  AdminActivityItem,
  AdminStudioIdentity,
} from '../types';

const API_BASE = '/api/admin';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const adminService = {
  async getDashboardMetrics(): Promise<AdminDashboardMetrics> {
    const res = await axios.get<ApiResponse<AdminDashboardMetrics>>(`${API_BASE}/dashboard`, {
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async getStudios(): Promise<AdminStudioItem[]> {
    const res = await axios.get<ApiResponse<AdminStudioItem[]>>(`${API_BASE}/studios`, {
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },

  async getFreelancers(): Promise<AdminFreelancerItem[]> {
    const res = await axios.get<ApiResponse<AdminFreelancerItem[]>>(`${API_BASE}/freelancers`, {
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },

  async getRequirements(): Promise<AdminRequirementItem[]> {
    const res = await axios.get<ApiResponse<AdminRequirementItem[]>>(`${API_BASE}/requirements`, {
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },

  async getRequests(): Promise<AdminRequestItem[]> {
    const res = await axios.get<ApiResponse<AdminRequestItem[]>>(`${API_BASE}/requests`, {
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },

  async getActivity(limit = 30): Promise<AdminActivityItem[]> {
    const res = await axios.get<ApiResponse<AdminActivityItem[]>>(`${API_BASE}/activity`, {
      params: { limit },
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },

  async getStudioIdentity(studioId: number): Promise<AdminStudioIdentity> {
    const res = await axios.get<ApiResponse<AdminStudioIdentity>>(`${API_BASE}/studios/${studioId}/identity`, {
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async getAllIdentitySubmissions(): Promise<AdminStudioIdentity[]> {
    const res = await axios.get<ApiResponse<AdminStudioIdentity[]>>(`${API_BASE}/identity-submissions`, {
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },
};
