import api from './api';
import {
  ApiResponse,
  RequirementStatus,
  WorkRequirement,
  WorkRequirementPayload,
  WorkRequirementSummary,
} from '../types';

export const requirementService = {
  // Create new requirement (WRK-001, WRK-002)
  async createRequirement(payload: WorkRequirementPayload): Promise<WorkRequirement> {
    const res = await api.post<ApiResponse<WorkRequirement>>('/requirements', payload);
    return res.data.data;
  },

  // Update existing requirement
  async updateRequirement(id: number, payload: WorkRequirementPayload): Promise<WorkRequirement> {
    const res = await api.put<ApiResponse<WorkRequirement>>(`/requirements/${id}`, payload);
    return res.data.data;
  },

  // Get requirement details (with privacy filtering)
  async getRequirementById(id: number): Promise<WorkRequirement> {
    const res = await api.get<ApiResponse<WorkRequirement>>(`/requirements/${id}`);
    return res.data.data;
  },

  // Get current studio's requirements list
  async getMyRequirements(status?: RequirementStatus): Promise<WorkRequirementSummary[]> {
    const res = await api.get<ApiResponse<WorkRequirementSummary[]>>('/requirements/studio/me', {
      params: { status },
    });
    return res.data.data;
  },

  // Update requirement lifecycle status (WRK-003)
  async updateRequirementStatus(id: number, status: RequirementStatus): Promise<WorkRequirement> {
    const res = await api.patch<ApiResponse<WorkRequirement>>(`/requirements/${id}/status`, { status });
    return res.data.data;
  },

  // Delete requirement
  async deleteRequirement(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/requirements/${id}`);
  },

  // Open requirements feed
  async getOpenRequirements(fromDate?: string, location?: string): Promise<WorkRequirementSummary[]> {
    const res = await api.get<ApiResponse<WorkRequirementSummary[]>>('/requirements/open', {
      params: { fromDate, location },
    });
    return res.data.data;
  },

  // AI-Ranked matching freelancers for requirement (DIS-002, DIS-006, WRK-005)
  async getMatchingFreelancers(id: number): Promise<import('../types').FreelancerCard[]> {
    const res = await api.get<ApiResponse<import('../types').FreelancerCard[]>>(`/requirements/${id}/matching-freelancers`);
    return res.data.data;
  },
};
