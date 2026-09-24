export type UserRole = 'STUDIO' | 'FREELANCER' | 'ADMIN';

export interface HealthStatus {
  status: string;
  service: string;
  version: string;
  timestamp?: string;
}

export interface UserSummary {
  id: number;
  email: string;
  role: UserRole;
  onboardingCompleted: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: UserSummary;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

export interface OnboardingStatusResponse {
  userId: number;
  email: string;
  role: UserRole;
  onboardingCompleted: boolean;
  message: string;
  nextStep: string;
}
