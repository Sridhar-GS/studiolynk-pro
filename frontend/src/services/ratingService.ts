import axios from 'axios';
import { ApiResponse, RatingDto, RatingSubmissionDto, RatingSummaryDto, RequirementRatingStatusDto } from '../types';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const ratingService = {
  async submitRating(requirementId: number, data: RatingSubmissionDto): Promise<RatingDto> {
    const res = await axios.post<ApiResponse<RatingDto>>(
      `/api/requirements/${requirementId}/ratings`,
      data,
      { headers: getAuthHeaders() }
    );
    return res.data.data;
  },

  async getRequirementRatingStatus(requirementId: number): Promise<RequirementRatingStatusDto> {
    const res = await axios.get<ApiResponse<RequirementRatingStatusDto>>(
      `/api/requirements/${requirementId}/ratings/status`,
      { headers: getAuthHeaders() }
    );
    return res.data.data;
  },

  async getFreelancerRatings(freelancerId: number): Promise<RatingSummaryDto> {
    const res = await axios.get<ApiResponse<RatingSummaryDto>>(
      `/api/freelancers/${freelancerId}/ratings`,
      { headers: getAuthHeaders() }
    );
    return res.data.data || { averageRating: 0, totalRatings: 0, ratings: [] };
  },

  async getStudioRatings(studioId: number): Promise<RatingSummaryDto> {
    const res = await axios.get<ApiResponse<RatingSummaryDto>>(
      `/api/studios/${studioId}/ratings`,
      { headers: getAuthHeaders() }
    );
    return res.data.data || { averageRating: 0, totalRatings: 0, ratings: [] };
  },
};
