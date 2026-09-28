import api from './api';
import {
  ApiResponse,
  FreelancerCard,
  FreelancerOnboardingPayload,
  FreelancerProfile,
  FreelancerSearchFilter,
  FreelancerSearchResponse,
  FreelancerUpdatePayload,
} from '../types';

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

  // Phase 8: Discovery (DIS-001 - DIS-006)
  async searchFreelancers(filters?: FreelancerSearchFilter): Promise<FreelancerSearchResponse> {
    const res = await api.get<ApiResponse<FreelancerSearchResponse>>('/freelancers/search', {
      params: filters,
    });
    return res.data.data;
  },

  async getFreelancerCard(id: number, date?: string, startTime?: string, endTime?: string): Promise<FreelancerCard> {
    const res = await api.get<ApiResponse<FreelancerCard>>(`/freelancers/${id}/card`, {
      params: { date, startTime, endTime },
    });
    return res.data.data;
  },
};

