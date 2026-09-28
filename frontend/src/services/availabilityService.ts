import api from './api';
import {
  ApiResponse,
  AvailabilityCheckRequest,
  AvailabilityCheckResponse,
  AvailabilityWindowResponse,
  UpdateAvailabilityPayload,
} from '../types';

export const availabilityService = {
  // Get authenticated freelancer's rolling 10-day availability (AVL-001)
  async getMyAvailability(): Promise<AvailabilityWindowResponse> {
    const res = await api.get<ApiResponse<AvailabilityWindowResponse>>('/freelancers/me/availability');
    return res.data.data;
  },

  // Update authenticated freelancer's availability slots (AVL-002, AVL-003)
  async updateMyAvailability(payload: UpdateAvailabilityPayload): Promise<AvailabilityWindowResponse> {
    const res = await api.put<ApiResponse<AvailabilityWindowResponse>>('/freelancers/me/availability', payload);
    return res.data.data;
  },

  // Reset 10-day rolling window back to NOT_SET
  async resetMyAvailability(): Promise<AvailabilityWindowResponse> {
    const res = await api.post<ApiResponse<AvailabilityWindowResponse>>('/freelancers/me/availability/reset');
    return res.data.data;
  },

  // Get any freelancer's 10-day availability window by ID (AVL-001 - AVL-006)
  async getFreelancerAvailability(freelancerId: number): Promise<AvailabilityWindowResponse> {
    const res = await api.get<ApiResponse<AvailabilityWindowResponse>>(`/freelancers/${freelancerId}/availability`);
    return res.data.data;
  },

  // Check shoot slot availability against candidate freelancer (AVL-004 - AVL-006)
  async checkAvailability(payload: AvailabilityCheckRequest): Promise<AvailabilityCheckResponse> {
    const res = await api.post<ApiResponse<AvailabilityCheckResponse>>('/freelancers/availability/check', payload);
    return res.data.data;
  },
};
