import api from './api';
import {
  ApiResponse,
  CreateWorkRequestPayload,
  RequestStatus,
  WorkRequest,
  WorkRequestSummary,
} from '../types';

export const requestService = {
  // Studio sends a work request to a freelancer (REQ-001, WRK-006)
  async createRequest(payload: CreateWorkRequestPayload): Promise<WorkRequest> {
    const res = await api.post<ApiResponse<WorkRequest>>('/requests', payload);
    return res.data.data;
  },

  // Studio retrieves their sent work requests
  async getStudioRequests(requirementId?: number, status?: RequestStatus): Promise<WorkRequestSummary[]> {
    const res = await api.get<ApiResponse<WorkRequestSummary[]>>('/requests/studio', {
      params: { requirementId, status },
    });
    return res.data.data;
  },

  // Freelancer retrieves their received work requests (REQ-002: private client contact masked)
  async getFreelancerRequests(status?: RequestStatus): Promise<WorkRequestSummary[]> {
    const res = await api.get<ApiResponse<WorkRequestSummary[]>>('/requests/freelancer', {
      params: { status },
    });
    return res.data.data;
  },

  // Get request details with dynamic privacy gating (REQ-002, REQ-006)
  async getRequestById(id: number): Promise<WorkRequest> {
    const res = await api.get<ApiResponse<WorkRequest>>(`/requests/${id}`);
    return res.data.data;
  },

  // Freelancer accepts request (REQ-004)
  async acceptRequest(id: number, agreedPrice?: number): Promise<WorkRequest> {
    const res = await api.patch<ApiResponse<WorkRequest>>(`/requests/${id}/accept`, {
      agreedPrice,
    });
    return res.data.data;
  },

  // Freelancer declines request (REQ-004)
  async rejectRequest(id: number, reason?: string): Promise<WorkRequest> {
    const res = await api.patch<ApiResponse<WorkRequest>>(`/requests/${id}/reject`, {
      reason,
    });
    return res.data.data;
  },

  // Studio confirms the final freelancer (REQ-005, WRK-007, WRK-008: closes other candidates, REQ-006: unlocks client contact)
  async confirmRequest(id: number): Promise<WorkRequest> {
    const res = await api.patch<ApiResponse<WorkRequest>>(`/requests/${id}/confirm`);
    return res.data.data;
  },

  // Either party cancels with a reason (REQ-007)
  async cancelRequest(id: number, reason: string): Promise<WorkRequest> {
    const res = await api.patch<ApiResponse<WorkRequest>>(`/requests/${id}/cancel`, {
      reason,
    });
    return res.data.data;
  },
};
