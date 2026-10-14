package com.studiolynk.service.impl;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.admin.AdminActivityItemDto;
import com.studiolynk.model.dto.admin.AdminDashboardMetricsDto;
import com.studiolynk.model.dto.admin.AdminFreelancerItemDto;
import com.studiolynk.model.dto.admin.AdminRequestItemDto;
import com.studiolynk.model.dto.admin.AdminRequirementItemDto;
import com.studiolynk.model.dto.admin.AdminStudioIdentityDto;
import com.studiolynk.model.dto.admin.AdminStudioItemDto;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.Notification;
import com.studiolynk.model.entity.Rating;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.model.entity.StudioIdentitySubmission;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.entity.WorkRequest;
import com.studiolynk.model.enums.RatingTargetType;
import com.studiolynk.model.enums.RequestStatus;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.NotificationRepository;
import com.studiolynk.repository.RatingRepository;
import com.studiolynk.repository.StudioIdentitySubmissionRepository;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.repository.WorkRequestRepository;
import com.studiolynk.repository.WorkRequirementRepository;
import com.studiolynk.service.AdminService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of Admin platform monitoring service (ADM-001 - ADM-006).
 * Strictly view-only; zero mutation/edit/delete operations provided.
 */
@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final StudioRepository studioRepository;
    private final FreelancerRepository freelancerRepository;
    private final WorkRequirementRepository workRequirementRepository;
    private final WorkRequestRepository workRequestRepository;
    private final RatingRepository ratingRepository;
    private final NotificationRepository notificationRepository;
    private final StudioIdentitySubmissionRepository studioIdentitySubmissionRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            StudioRepository studioRepository,
            FreelancerRepository freelancerRepository,
            WorkRequirementRepository workRequirementRepository,
            WorkRequestRepository workRequestRepository,
            RatingRepository ratingRepository,
            NotificationRepository notificationRepository,
            StudioIdentitySubmissionRepository studioIdentitySubmissionRepository) {
        this.userRepository = userRepository;
        this.studioRepository = studioRepository;
        this.freelancerRepository = freelancerRepository;
        this.workRequirementRepository = workRequirementRepository;
        this.workRequestRepository = workRequestRepository;
        this.ratingRepository = ratingRepository;
        this.notificationRepository = notificationRepository;
        this.studioIdentitySubmissionRepository = studioIdentitySubmissionRepository;
    }

    @Override
    public AdminDashboardMetricsDto getDashboardMetrics() {
        AdminDashboardMetricsDto metrics = new AdminDashboardMetricsDto();

        // User & Profile Counters
        long totalStudios = studioRepository.count();
        long totalFreelancers = freelancerRepository.count();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::isOnboardingCompleted).count();

        metrics.setTotalStudios(totalStudios);
        metrics.setTotalFreelancers(totalFreelancers);
        metrics.setTotalUsers(totalUsers);
        metrics.setActiveUsers(activeUsers);

        // Requirement Counters
        List<WorkRequirement> allReqs = workRequirementRepository.findAll();
        metrics.setTotalRequirements(allReqs.size());
        metrics.setOpenRequirements(allReqs.stream().filter(r -> r.getStatus() == RequirementStatus.OPEN).count());
        metrics.setConfirmedRequirements(allReqs.stream().filter(r -> r.getStatus() == RequirementStatus.CONFIRMED).count());
        metrics.setCompletedRequirements(allReqs.stream().filter(r -> r.getStatus() == RequirementStatus.COMPLETED).count());

        // Request Counters
        List<WorkRequest> allRequests = workRequestRepository.findAll();
        metrics.setTotalRequests(allRequests.size());
        metrics.setPendingRequests(allRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count());
        metrics.setAcceptedRequests(allRequests.stream().filter(r -> r.getStatus() == RequestStatus.ACCEPTED).count());
        metrics.setConfirmedRequests(allRequests.stream().filter(r -> r.getStatus() == RequestStatus.CONFIRMED).count());

        // Ratings & Quality Metrics
        List<Rating> allRatings = ratingRepository.findAll();
        metrics.setTotalRatings(allRatings.size());
        double avgRating = allRatings.isEmpty() ? 0.0 :
                allRatings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        metrics.setAveragePlatformRating(Math.round(avgRating * 10.0) / 10.0);

        // Identity Verification Monitoring (ADM-005)
        long totalIdentities = studioIdentitySubmissionRepository.count();
        metrics.setTotalIdentitySubmissions(totalIdentities);

        List<AdminStudioIdentityDto> recentIdentities = studioIdentitySubmissionRepository
                .findAll(Sort.by(Sort.Direction.DESC, "submittedAt"))
                .stream()
                .limit(5)
                .map(this::mapToStudioIdentityDto)
                .collect(Collectors.toList());
        metrics.setRecentIdentitySubmissions(recentIdentities);

        // Platform Activity Feed
        metrics.setRecentActivity(getPlatformActivity(12));

        return metrics;
    }

    @Override
    public List<AdminStudioItemDto> getAllStudios() {
        return studioRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToStudioItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminFreelancerItemDto> getAllFreelancers() {
        return freelancerRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToFreelancerItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminRequirementItemDto> getAllRequirements() {
        return workRequirementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToRequirementItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminRequestItemDto> getAllRequests() {
        return workRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToRequestItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminActivityItemDto> getPlatformActivity(int limit) {
        List<AdminActivityItemDto> activityStream = new ArrayList<>();

        // 1. Collect recent in-app notifications
        List<Notification> notifications = notificationRepository.findAll(
                PageRequest.of(0, Math.max(limit, 20), Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        for (Notification n : notifications) {
            String actorEmail = n.getUser() != null ? n.getUser().getEmail() : "system@studiolynk.com";
            String actorRole = n.getUser() != null ? n.getUser().getRole().name() : "SYSTEM";
            activityStream.add(new AdminActivityItemDto(
                    n.getId(),
                    n.getCreatedAt(),
                    n.getType() != null ? n.getType().name() : "NOTIFICATION",
                    n.getTitle(),
                    n.getMessage(),
                    actorEmail,
                    actorRole,
                    "NOTIFICATION",
                    n.getRelatedEntityId()
            ));
        }

        // 2. Collect recent identity submissions (ADM-005)
        List<StudioIdentitySubmission> submissions = studioIdentitySubmissionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "submittedAt")
        );
        for (StudioIdentitySubmission s : submissions) {
            LocalDateTime ts = s.getSubmittedAt() != null ?
                    LocalDateTime.ofInstant(s.getSubmittedAt(), ZoneId.systemDefault()) :
                    LocalDateTime.now();
            String studioEmail = s.getStudio() != null && s.getStudio().getUser() != null ?
                    s.getStudio().getUser().getEmail() : "unknown";
            String studioName = s.getStudio() != null ? s.getStudio().getStudioName() : "Studio";

            activityStream.add(new AdminActivityItemDto(
                    s.getId() + 100000L,
                    ts,
                    "IDENTITY_SUBMITTED",
                    "Studio Identity Declared",
                    studioName + " registered " + s.getDocumentType() + " declaration",
                    studioEmail,
                    "STUDIO",
                    "STUDIO_IDENTITY",
                    s.getStudio() != null ? s.getStudio().getId() : null
            ));
        }

        // Sort descending by timestamp and truncate to limit
        return activityStream.stream()
                .sorted(Comparator.comparing(AdminActivityItemDto::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminStudioIdentityDto> getAllStudioIdentities() {
        return studioIdentitySubmissionRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"))
                .stream()
                .map(this::mapToStudioIdentityDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminStudioIdentityDto getStudioIdentity(Long studioId) {
        StudioIdentitySubmission submission = studioIdentitySubmissionRepository
                .findTopByStudioIdOrderBySubmittedAtDesc(studioId)
                .orElseThrow(() -> new ResourceNotFoundException("No identity submission found for studio id: " + studioId));
        return mapToStudioIdentityDto(submission);
    }

    // Helper mapping methods

    private AdminStudioItemDto mapToStudioItemDto(Studio studio) {
        String email = studio.getUser() != null ? studio.getUser().getEmail() : "";
        boolean onboardingCompleted = studio.getUser() != null && studio.getUser().isOnboardingCompleted();

        long totalReqs = workRequirementRepository.countByStudioId(studio.getId());
        long completedShoots = workRequirementRepository.countByStudioIdAndStatus(studio.getId(), RequirementStatus.COMPLETED);

        Optional<StudioIdentitySubmission> identity = studioIdentitySubmissionRepository
                .findTopByStudioIdOrderBySubmittedAtDesc(studio.getId());

        String identityStatus = identity.map(i -> i.getStatus().name()).orElse("NOT_SUBMITTED");
        Instant identitySubmittedAt = identity.map(StudioIdentitySubmission::getSubmittedAt).orElse(null);

        return new AdminStudioItemDto(
                studio.getId(),
                studio.getUser() != null ? studio.getUser().getId() : null,
                studio.getStudioName(),
                studio.getOwnerName(),
                email,
                studio.getPhone(),
                studio.getAddress(),
                studio.getLatitude(),
                studio.getLongitude(),
                studio.getYearsOfOperation(),
                studio.getLogoUrl(),
                onboardingCompleted,
                studio.getCreatedAt(),
                totalReqs,
                completedShoots,
                identityStatus,
                identitySubmittedAt
        );
    }

    private AdminFreelancerItemDto mapToFreelancerItemDto(Freelancer freelancer) {
        String email = freelancer.getUser() != null ? freelancer.getUser().getEmail() : "";
        boolean onboardingCompleted = freelancer.getUser() != null && freelancer.getUser().isOnboardingCompleted();

        List<String> skills = freelancer.getSkills().stream().map(Skill::getName).collect(Collectors.toList());
        List<String> services = freelancer.getServices().stream().map(ServiceEntity::getName).collect(Collectors.toList());
        List<String> equipment = freelancer.getEquipment().stream().map(Equipment::getName).collect(Collectors.toList());

        long totalRequests = workRequestRepository.findByFreelancerId(freelancer.getId()).size();
        long completedShoots = workRequirementRepository.findAll().stream()
                .filter(req -> req.getStatus() == RequirementStatus.COMPLETED &&
                        req.getConfirmedFreelancer() != null &&
                        req.getConfirmedFreelancer().getId().equals(freelancer.getId()))
                .count();

        Double avgRating = 0.0;
        Long ratingCount = 0L;
        if (freelancer.getUser() != null) {
            avgRating = ratingRepository.findAverageScoreByToUserIdAndTargetType(
                    freelancer.getUser().getId(),
                    RatingTargetType.FREELANCER
            );
            ratingCount = ratingRepository.countByToUserIdAndTargetType(
                    freelancer.getUser().getId(),
                    RatingTargetType.FREELANCER
            );
        }

        return new AdminFreelancerItemDto(
                freelancer.getId(),
                freelancer.getUser() != null ? freelancer.getUser().getId() : null,
                freelancer.getFullName(),
                email,
                freelancer.getPhone(),
                freelancer.getAddress(),
                freelancer.getLatitude(),
                freelancer.getLongitude(),
                freelancer.getExperienceYears(),
                freelancer.getFullDayRate(),
                freelancer.getHalfDayRate(),
                freelancer.getBio(),
                freelancer.getProfilePhotoUrl(),
                skills,
                services,
                equipment,
                onboardingCompleted,
                freelancer.getCreatedAt(),
                totalRequests,
                completedShoots,
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                ratingCount != null ? ratingCount : 0L
        );
    }

    private AdminRequirementItemDto mapToRequirementItemDto(WorkRequirement req) {
        Long studioId = req.getStudio() != null ? req.getStudio().getId() : null;
        String studioName = req.getStudio() != null ? req.getStudio().getStudioName() : "Studio";
        String studioEmail = (req.getStudio() != null && req.getStudio().getUser() != null) ?
                req.getStudio().getUser().getEmail() : "";

        Long confirmedFreelancerId = req.getConfirmedFreelancer() != null ?
                req.getConfirmedFreelancer().getId() : null;
        String confirmedFreelancerName = req.getConfirmedFreelancer() != null ?
                req.getConfirmedFreelancer().getFullName() : null;

        long reqCount = workRequestRepository.findByRequirementId(req.getId()).size();

        List<String> requiredSkills = req.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

        List<String> requiredServices = req.getRequiredServices().stream()
                .map(ServiceEntity::getName)
                .collect(Collectors.toList());

        return new AdminRequirementItemDto(
                req.getId(),
                studioId,
                studioName,
                studioEmail,
                req.getEventName(),
                req.getEventType(),
                req.getEventDate(),
                req.getStartTime(),
                req.getEndTime(),
                req.getLocation(),
                req.getDayType() != null ? req.getDayType().name() : null,
                req.getBudget(),
                req.getStatus() != null ? req.getStatus().name() : null,
                req.getDescription(),
                confirmedFreelancerId,
                confirmedFreelancerName,
                reqCount,
                requiredSkills,
                requiredServices,
                req.getCreatedAt()
        );
    }

    private AdminRequestItemDto mapToRequestItemDto(WorkRequest r) {
        Long reqId = r.getRequirement() != null ? r.getRequirement().getId() : null;
        String eventName = r.getRequirement() != null ? r.getRequirement().getEventName() : "Shoot";
        java.time.LocalDate eventDate = r.getRequirement() != null ? r.getRequirement().getEventDate() : null;

        Long studioId = null;
        String studioName = "Studio";
        if (r.getRequirement() != null && r.getRequirement().getStudio() != null) {
            studioId = r.getRequirement().getStudio().getId();
            studioName = r.getRequirement().getStudio().getStudioName();
        }

        Long freelancerId = r.getFreelancer() != null ? r.getFreelancer().getId() : null;
        String freelancerName = r.getFreelancer() != null ? r.getFreelancer().getFullName() : "Creator";
        String freelancerEmail = (r.getFreelancer() != null && r.getFreelancer().getUser() != null) ?
                r.getFreelancer().getUser().getEmail() : "";

        return new AdminRequestItemDto(
                r.getId(),
                reqId,
                eventName,
                eventDate,
                studioId,
                studioName,
                freelancerId,
                freelancerName,
                freelancerEmail,
                r.getAgreedPrice(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getMessage(),
                r.getCancellationReason(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private AdminStudioIdentityDto mapToStudioIdentityDto(StudioIdentitySubmission sub) {
        Long studioId = sub.getStudio() != null ? sub.getStudio().getId() : null;
        String studioName = sub.getStudio() != null ? sub.getStudio().getStudioName() : "";
        String ownerName = sub.getStudio() != null ? sub.getStudio().getOwnerName() : "";
        String studioEmail = (sub.getStudio() != null && sub.getStudio().getUser() != null) ?
                sub.getStudio().getUser().getEmail() : "";
        String phone = sub.getStudio() != null ? sub.getStudio().getPhone() : "";

        return new AdminStudioIdentityDto(
                sub.getId(),
                studioId,
                studioName,
                ownerName,
                studioEmail,
                phone,
                sub.getDocumentType(),
                sub.getDocumentUrl(),
                sub.getDeclarationText(),
                sub.getStatus() != null ? sub.getStatus().name() : "SUBMITTED",
                sub.getSubmittedAt()
        );
    }
}
