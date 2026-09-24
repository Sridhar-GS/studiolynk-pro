export type UserRole = 'STUDIO' | 'FREELANCER' | 'ADMIN';

export interface HealthStatus {
  status: string;
  service: string;
  version: string;
  timestamp?: string;
}

export interface UserSession {
  userId: number;
  email: string;
  role: UserRole;
  onboardingCompleted: boolean;
  token?: string;
}
