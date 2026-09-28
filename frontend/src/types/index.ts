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

// -----------------------------------------------------------------------------
// Portfolio & S3 Upload Types (Phase 6)
// -----------------------------------------------------------------------------
export interface PortfolioImage {
  id: number;
  categoryId: number;
  s3Key: string;
  imageUrl: string;
  originalFilename?: string;
  contentType?: string;
  fileSize?: number;
  sortOrder: number;
  createdAt?: string;
}

export interface PortfolioCategory {
  id: number;
  portfolioId: number;
  name: string;
  sortOrder: number;
  createdAt?: string;
  images: PortfolioImage[];
  imageCount: number;
}

export interface Portfolio {
  id: number;
  freelancerId: number;
  freelancerName?: string;
  categories: PortfolioCategory[];
  totalCategories: number;
  totalImages: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCategoryPayload {
  name: string;
  sortOrder?: number;
}

export interface UpdateCategoryPayload {
  name: string;
  sortOrder?: number;
}

export interface ReorderItemsPayload {
  itemIds: number[];
}

export interface UploadResponse {
  s3Key: string;
  imageUrl: string;
  originalFilename?: string;
  contentType?: string;
  fileSize?: number;
}

// -----------------------------------------------------------------------------
// Availability Domain Types (Phase 7 - AVL-001 to AVL-009)
// -----------------------------------------------------------------------------
export type AvailabilityStatus = 'AVAILABLE' | 'BUSY' | 'NOT_SET';

export interface AvailabilitySlot {
  id?: number | null;
  date: string; // YYYY-MM-DD
  dayOfWeek: string; // Mon, Tue, etc.
  dayOfMonth: number;
  month: string; // Sep, Oct, etc.
  status: AvailabilityStatus;
  startTime?: string | null; // HH:mm
  endTime?: string | null; // HH:mm
  formattedTime?: string | null;
  isWithinWindow?: boolean;
  isAvailable?: boolean;
  withinWindow?: boolean;
  available?: boolean;
}

export interface AvailabilityWindowResponse {
  freelancerId: number;
  freelancerName: string;
  windowStartDate: string;
  windowEndDate: string;
  totalDays: number;
  availableDaysCount: number;
  busyDaysCount: number;
  notSetDaysCount: number;
  slots: AvailabilitySlot[];
}

export interface UpdateAvailabilityItem {
  date: string;
  status: AvailabilityStatus;
  startTime?: string | null;
  endTime?: string | null;
}

export interface UpdateAvailabilityPayload {
  availability: UpdateAvailabilityItem[];
}

export interface AvailabilityCheckRequest {
  freelancerId?: number;
  date: string;
  startTime?: string;
  endTime?: string;
}

export interface AvailabilityCheckResponse {
  freelancerId: number;
  date: string;
  requestedStartTime?: string;
  requestedEndTime?: string;
  withinWindow: boolean;
  status: AvailabilityStatus;
  availableStartTime?: string;
  availableEndTime?: string;
  match: boolean;
  reason: string;
}

// -----------------------------------------------------------------------------
// Freelancer Discovery Types (Phase 8 - DIS-001 to DIS-006)
// -----------------------------------------------------------------------------
export interface FreelancerCard {
  id: number;
  userId: number;
  fullName: string;
  profilePhotoUrl?: string;
  phone?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  distanceKm?: number;
  formattedDistance?: string;
  experienceYears?: number;
  bio?: string;
  fullDayRate?: number;
  halfDayRate?: number;
  averageRating: number;
  reviewCount: number;
  skills: Skill[];
  services: ServiceItem[];
  equipment: EquipmentItem[];
  primaryRole?: string;
  availabilityStatus?: AvailabilityStatus;
  availableHours?: string;
  withinWindow?: boolean;
  availabilityNotice?: string;
  aiMatchScore?: number;
  onboardingCompleted: boolean;
}

export interface FreelancerSearchFilter {
  keyword?: string;
  serviceId?: number;
  serviceName?: string;
  skillId?: number;
  skillName?: string;
  equipmentId?: number;
  equipmentName?: string;
  location?: string;
  latitude?: number;
  longitude?: number;
  maxDistanceKm?: number;
  date?: string; // YYYY-MM-DD
  startTime?: string; // HH:mm
  endTime?: string; // HH:mm
  dayType?: 'FULL_DAY' | 'HALF_DAY';
  maxBudget?: number;
  minExperience?: number;
  sortBy?: 'relevance' | 'distance' | 'rating' | 'experience' | 'price_asc' | 'price_desc';
}

export interface FreelancerSearchResponse {
  totalResults: number;
  searchedDate?: string;
  searchedTime?: string;
  searchedLocation?: string;
  freelancers: FreelancerCard[];
}

// -----------------------------------------------------------------------------
// Work Requirements Domain Types (Phase 9 - WRK-001 to WRK-008)
// -----------------------------------------------------------------------------
export type RequirementStatus =
  | 'DRAFT'
  | 'OPEN'
  | 'REQUESTED'
  | 'ACCEPTED'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface WorkRequirement {
  id: number;
  studioId: number;
  studioName: string;
  studioLogoUrl?: string;
  studioPhone?: string;
  eventName: string;
  eventType: string;
  eventDate: string; // YYYY-MM-DD
  startTime: string; // HH:mm
  endTime: string; // HH:mm
  formattedTime?: string;
  location: string;
  latitude?: number;
  longitude?: number;
  dayType: 'FULL_DAY' | 'HALF_DAY';
  budget: number;
  description?: string;
  status: RequirementStatus;
  eventContactName?: string;
  eventContactPhone?: string;
  hasPrivateContactDetails: boolean;
  privateDetailsRevealed: boolean;
  confirmedFreelancerId?: number;
  confirmedFreelancerName?: string;
  confirmedFreelancerPhotoUrl?: string;
  requiredSkills: Skill[];
  requiredServices: ServiceItem[];
  requiredEquipment: EquipmentItem[];
  activeRequestsCount?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface WorkRequirementSummary {
  id: number;
  studioId: number;
  studioName: string;
  studioLogoUrl?: string;
  eventName: string;
  eventType: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  formattedTime?: string;
  location: string;
  dayType: 'FULL_DAY' | 'HALF_DAY';
  budget: number;
  status: RequirementStatus;
  skillsCount: number;
  servicesCount: number;
  equipmentCount: number;
  confirmedFreelancerId?: number;
  confirmedFreelancerName?: string;
}

export interface WorkRequirementPayload {
  eventName: string;
  eventType: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  location: string;
  latitude?: number;
  longitude?: number;
  dayType?: 'FULL_DAY' | 'HALF_DAY';
  budget: number;
  description?: string;
  status?: RequirementStatus;
  eventContactName?: string;
  eventContactPhone?: string;
  requiredSkillIds?: number[];
  requiredServiceIds?: number[];
  requiredEquipmentIds?: number[];
}

// -----------------------------------------------------------------------------
// Work Requests Domain Types (Phase 10 - REQ-001 to REQ-009, WRK-006 to WRK-008)
// -----------------------------------------------------------------------------
export type RequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CONFIRMED' | 'CANCELLED';

export interface WorkRequest {
  id: number;
  requirementId: number;
  freelancerId: number;
  freelancerName: string;
  freelancerPhotoUrl?: string;
  freelancerPhone?: string;
  status: RequestStatus;
  agreedPrice?: number;
  message?: string;
  cancellationReason?: string;

  studioId: number;
  studioName: string;
  studioLogoUrl?: string;
  studioPhone?: string;

  eventName: string;
  eventType: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  formattedTime?: string;
  location: string;
  dayType: 'FULL_DAY' | 'HALF_DAY';
  budget: number;
  description?: string;

  eventContactName?: string;
  eventContactPhone?: string;
  hasPrivateContactDetails: boolean;
  privateDetailsRevealed: boolean;

  requiredSkills: Skill[];
  requiredServices: ServiceItem[];
  requiredEquipment: EquipmentItem[];

  createdAt: string;
  updatedAt?: string;
}

export interface WorkRequestSummary {
  id: number;
  requirementId: number;
  eventName: string;
  eventType: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  formattedTime?: string;
  location: string;
  dayType: 'FULL_DAY' | 'HALF_DAY';
  budget: number;

  freelancerId: number;
  freelancerName: string;
  freelancerPhotoUrl?: string;
  freelancerCity?: string;
  freelancerPrimarySkill?: string;

  studioId: number;
  studioName: string;
  studioLogoUrl?: string;

  status: RequestStatus;
  agreedPrice?: number;
  message?: string;
  cancellationReason?: string;
  createdAt: string;
}

export interface CreateWorkRequestPayload {
  requirementId: number;
  freelancerId: number;
  offeredPrice?: number;
  message?: string;
}

