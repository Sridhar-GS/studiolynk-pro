import api from './api';
import { ApiResponse, FreelancerOnboardingPayload, FreelancerProfile, FreelancerUpdatePayload } from '../types';

export const freelancerService = {
  async getProfile(): Promise<FreelancerProfile> {
    const res = await api.get<ApiResponse<FreelancerProfile>>('/freelancers/me');
    return res.data.data;
  },

  async getProfileById(id: number): Promise<FreelancerProfile> {
    const res = await api.get<ApiResponse<FreelancerProfile>>(`/freelancers/${id}`);
    return res.data.data;
  },

  async submitOnboarding(payload: FreelancerOnboardingPayload): Promise<FreelancerProfile> {
    const res = await api.post<ApiResponse<FreelancerProfile>>('/onboarding/freelancer', payload);
    return res.data.data;
  },

  async saveDraft(payload: FreelancerOnboardingPayload): Promise<FreelancerProfile> {
    const res = await api.put<ApiResponse<FreelancerProfile>>('/onboarding/freelancer', payload);
    return res.data.data;
  },

  async updateProfile(payload: FreelancerUpdatePayload): Promise<FreelancerProfile> {
    const res = await api.put<ApiResponse<FreelancerProfile>>('/freelancers/me', payload);
    return res.data.data;
  },
};
