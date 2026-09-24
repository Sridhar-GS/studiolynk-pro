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
  redirectUrl?: string;
  message?: string;
  nextStep?: string;
}

// -----------------------------------------------------------------------------
// Studio Types (Phase 4)
// -----------------------------------------------------------------------------
export interface StudioSocialLink {
  id?: number;
  platformName: string;
  url: string;
}

export interface StudioIdentitySubmission {
  id?: number;
  documentType: string;
  documentUrl?: string;
  declarationText?: string;
  status: 'SUBMITTED' | 'VERIFIED';
  submittedAt?: string;
}

export interface StudioProfile {
  id: number;
  userId: number;
  email: string;
  studioName: string;
  ownerName: string;
  logoUrl?: string;
  phone: string;
  address: string;
  latitude?: number;
  longitude?: number;
  yearsOfOperation: number;
  onboardingCompleted: boolean;
  completionPercentage: number;
  socialLinks: StudioSocialLink[];
  identitySubmission?: StudioIdentitySubmission;
  createdAt?: string;
  updatedAt?: string;
}

export interface StudioOnboardingPayload {
  studioName: string;
  ownerName: string;
  phone: string;
  address: string;
  latitude?: number;
  longitude?: number;
  yearsOfOperation?: number;
  logoUrl?: string;
  socialLinks?: StudioSocialLink[];
  documentType?: string;
  documentUrl?: string;
  declarationText?: string;
}

export interface StudioUpdatePayload {
  studioName: string;
  ownerName: string;
  phone: string;
  address: string;
  latitude?: number;
  longitude?: number;
  yearsOfOperation?: number;
  logoUrl?: string;
  socialLinks?: StudioSocialLink[];
}

// -----------------------------------------------------------------------------
// Catalogue & Freelancer Types (Phase 5)
// -----------------------------------------------------------------------------
export interface Skill {
  id: number;
  name: string;
  custom?: boolean;
}

export interface ServiceItem {
  id: number;
  name: string;
  custom?: boolean;
}

export interface EquipmentCategory {
  id: number;
  name: string;
  description?: string;
}

export interface EquipmentItem {
  id: number;
  categoryId?: number;
  categoryName?: string;
  name: string;
  custom?: boolean;
}

export interface FreelancerProfile {
  id: number;
  userId: number;
  email: string;
  fullName: string;
  profilePhotoUrl?: string;
  phone: string;
  address: string;
  latitude?: number;
  longitude?: number;
  experienceYears: number;
  bio?: string;
  fullDayRate?: number;
  halfDayRate?: number;
  onboardingCompleted: boolean;
  completionPercentage: number;
  skills: Skill[];
  services: ServiceItem[];
  equipment: EquipmentItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface FreelancerOnboardingPayload {
  fullName: string;
  phone: string;
  address: string;
  latitude?: number;
  longitude?: number;
  experienceYears?: number;
  bio?: string;
  profilePhotoUrl?: string;
  fullDayRate?: number;
  halfDayRate?: number;
  skillIds?: number[];
  serviceIds?: number[];
  equipmentIds?: number[];
}

export interface FreelancerUpdatePayload {
  fullName: string;
  phone: string;
  address: string;
  latitude?: number;
  longitude?: number;
  experienceYears?: number;
  bio?: string;
  profilePhotoUrl?: string;
  fullDayRate?: number;
  halfDayRate?: number;
  skillIds?: number[];
  serviceIds?: number[];
  equipmentIds?: number[];
}

