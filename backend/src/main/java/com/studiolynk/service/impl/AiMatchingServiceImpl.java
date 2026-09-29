package com.studiolynk.service.impl;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.AvailabilityCheckResponseDto;
import com.studiolynk.model.dto.EquipmentDto;
import com.studiolynk.model.dto.FreelancerCardDto;
import com.studiolynk.model.dto.ServiceDto;
import com.studiolynk.model.dto.SkillDto;
import com.studiolynk.model.dto.ml.CandidateBatchItemDto;
import com.studiolynk.model.dto.ml.MatchFeaturesDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.Portfolio;
import com.studiolynk.model.entity.PortfolioCategory;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.enums.AvailabilityStatus;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.PortfolioCategoryRepository;
import com.studiolynk.repository.PortfolioRepository;
import com.studiolynk.repository.RatingRepository;
import com.studiolynk.repository.WorkRequirementRepository;
import com.studiolynk.service.AiMatchingService;
import com.studiolynk.service.AvailabilityService;
import com.studiolynk.service.MlServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AiMatchingServiceImpl implements AiMatchingService {

    private static final Logger log = LoggerFactory.getLogger(AiMatchingServiceImpl.class);

    private final WorkRequirementRepository requirementRepository;
    private final FreelancerRepository freelancerRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioCategoryRepository categoryRepository;
    private final AvailabilityService availabilityService;
    private final RatingRepository ratingRepository;
    private final MlServiceClient mlServiceClient;

    public AiMatchingServiceImpl(
            WorkRequirementRepository requirementRepository,
            FreelancerRepository freelancerRepository,
            PortfolioRepository portfolioRepository,
            PortfolioCategoryRepository categoryRepository,
            AvailabilityService availabilityService,
            RatingRepository ratingRepository,
            MlServiceClient mlServiceClient) {
        this.requirementRepository = requirementRepository;
        this.freelancerRepository = freelancerRepository;
        this.portfolioRepository = portfolioRepository;
        this.categoryRepository = categoryRepository;
        this.availabilityService = availabilityService;
        this.ratingRepository = ratingRepository;
        this.mlServiceClient = mlServiceClient;
    }

    @Override
    @Transactional(readOnly = true)
    public MatchFeaturesDto extractFeatures(WorkRequirement requirement, Freelancer freelancer) {
        // 1. Skill Match (0.0 to 1.0)
        double skillMatch = calculateSkillMatch(requirement, freelancer);

        // 2. Portfolio Relevance (0.0 to 1.0) - ML-013, POR-009 structured categories and text, no CV
        double portfolioRelevance = calculatePortfolioRelevance(requirement, freelancer);

        // 3. Experience (0.0 to 1.0) - Normalized craft maturity
        double experience = calculateExperience(freelancer);

        // 4. Budget Compatibility (0.0 to 1.0) - Relation between studio budget and day rate
        double budgetCompatibility = calculateBudgetCompatibility(requirement, freelancer);

        // 5. Location Distance (0.0 to 1.0) - Haversine proximity score
        double locationDistance = calculateLocationDistance(requirement, freelancer);

        // 6. Availability/Time Compatibility (0.0 to 1.0) - Schedule compatibility
        double availabilityCompatibility = calculateAvailabilityCompatibility(requirement, freelancer);

        // RATINGS ARE STRICTLY EXCLUDED (ML-005, RAT-004)
        return new MatchFeaturesDto(
                round4(skillMatch),
                round4(portfolioRelevance),
                round4(experience),
                round4(budgetCompatibility),
                round4(locationDistance),
                round4(availabilityCompatibility)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FreelancerCardDto> getMatchingFreelancersForRequirement(Long requirementId) {
        WorkRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Work requirement not found with id: " + requirementId));

        // Get all completed-onboarding freelancers
        List<Freelancer> allFreelancers = freelancerRepository.findAll().stream()
                .filter(f -> f.getUser() != null && f.getUser().isOnboardingCompleted())
                .toList();

        List<Freelancer> eligibleFreelancers = new ArrayList<>();
        List<FreelancerCardDto> eligibleCards = new ArrayList<>();
        List<CandidateBatchItemDto> batchItems = new ArrayList<>();

        for (Freelancer f : allFreelancers) {
            // STEP 1: Hard availability filtering (ML-012, AVL-004, AVL-005)
            AvailabilityCheckResponseDto check = availabilityService.checkAvailability(
                    f.getId(), requirement.getEventDate(), requirement.getStartTime(), requirement.getEndTime());

            AvailabilityStatus slotStatus = check.getStatus();
            String availableHours = null;
            Boolean withinWindow = check.isWithinWindow();
            String availabilityNotice = null;

            if (check.isWithinWindow()) {
                // Inside 10-day window: Busy and Not Set MUST be strictly excluded (AVL-004, AVL-005, ML-012)
                if (!check.isMatch()) {
                    continue; // Exclude candidate
                }
                if (check.getAvailableStartTime() != null && check.getAvailableEndTime() != null) {
                    availableHours = check.getAvailableStartTime() + " - " + check.getAvailableEndTime();
                }
                availabilityNotice = "Verified available for shoot interval";
            } else {
                // Beyond 10-day window: retain candidate with unknown availability (AVL-006)
                slotStatus = AvailabilityStatus.NOT_SET;
                availabilityNotice = "Beyond 10-day scheduling window. Availability unknown.";
            }

            // Candidate passed hard filters: prepare card and features
            Double distanceKm = calculateHaversineKm(
                    requirement.getLatitude(), requirement.getLongitude(),
                    f.getLatitude(), f.getLongitude());

            FreelancerCardDto card = mapToCardDto(f, distanceKm, slotStatus, availableHours, withinWindow, availabilityNotice);
            MatchFeaturesDto features = extractFeatures(requirement, f);

            eligibleFreelancers.add(f);
            eligibleCards.add(card);
            batchItems.add(new CandidateBatchItemDto(f.getId(), features));
        }

        if (eligibleCards.isEmpty()) {
            return eligibleCards;
        }

        // STEP 2 & 3: Send eligible candidate features to ML service for vectorized scoring (ML-010, ML-011)
        Map<Long, Double> scores = mlServiceClient.predictBatch(batchItems);

        // STEP 4 & 5: Populate aiMatchScore and sort descending
        for (FreelancerCardDto card : eligibleCards) {
            Double score = scores.get(card.getId());
            if (score != null) {
                card.setAiMatchScore(score);
            } else {
                card.setAiMatchScore(50.0);
            }
        }

        eligibleCards.sort(Comparator.comparing(
                (FreelancerCardDto c) -> c.getAiMatchScore() != null ? c.getAiMatchScore() : 0.0).reversed());

        return eligibleCards;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FreelancerCardDto> scoreAndRankCandidates(WorkRequirement requirement, List<FreelancerCardDto> candidateCards) {
        if (candidateCards == null || candidateCards.isEmpty() || requirement == null) {
            return candidateCards != null ? candidateCards : new ArrayList<>();
        }

        List<CandidateBatchItemDto> batchItems = new ArrayList<>();
        for (FreelancerCardDto card : candidateCards) {
            Optional<Freelancer> fOpt = freelancerRepository.findById(card.getId());
            if (fOpt.isPresent()) {
                MatchFeaturesDto features = extractFeatures(requirement, fOpt.get());
                batchItems.add(new CandidateBatchItemDto(card.getId(), features));
            }
        }

        Map<Long, Double> scores = mlServiceClient.predictBatch(batchItems);
        for (FreelancerCardDto card : candidateCards) {
            Double score = scores.get(card.getId());
            if (score != null) {
                card.setAiMatchScore(score);
            }
        }

        candidateCards.sort(Comparator.comparing(
                (FreelancerCardDto c) -> c.getAiMatchScore() != null ? c.getAiMatchScore() : 0.0).reversed());

        return candidateCards;
    }

    // ==========================================
    // Feature Extraction Helpers
    // ==========================================

    private double calculateSkillMatch(WorkRequirement requirement, Freelancer freelancer) {
        Set<Skill> reqSkills = requirement.getRequiredSkills();
        if (reqSkills == null || reqSkills.isEmpty()) {
            return 1.0; // No skill constraints specified
        }

        Set<Skill> frlSkills = freelancer.getSkills();
        if (frlSkills == null || frlSkills.isEmpty()) {
            return 0.0;
        }

        Set<Long> frlSkillIds = frlSkills.stream().map(Skill::getId).collect(Collectors.toSet());
        Set<String> frlSkillNames = frlSkills.stream().map(s -> s.getName().toLowerCase().trim()).collect(Collectors.toSet());

        long matchCount = reqSkills.stream()
                .filter(s -> frlSkillIds.contains(s.getId()) || frlSkillNames.contains(s.getName().toLowerCase().trim()))
                .count();

        return (double) matchCount / reqSkills.size();
    }

    private double calculatePortfolioRelevance(WorkRequirement requirement, Freelancer freelancer) {
        // Structured categories and metadata/text (ML-013, POR-009)
        String eventType = requirement.getEventType() != null ? requirement.getEventType().toLowerCase().trim() : "";
        String desc = requirement.getDescription() != null ? requirement.getDescription().toLowerCase().trim() : "";

        Set<ServiceEntity> reqServices = requirement.getRequiredServices();
        Set<String> reqServiceNames = reqServices != null
                ? reqServices.stream().map(s -> s.getName().toLowerCase().trim()).collect(Collectors.toSet())
                : Set.of();

        Optional<Portfolio> portfolioOpt = portfolioRepository.findByFreelancerId(freelancer.getId());
        List<PortfolioCategory> categories = portfolioOpt.isPresent()
                ? categoryRepository.findByPortfolioIdOrderBySortOrderAsc(portfolioOpt.get().getId())
                : List.of();

        if (categories.isEmpty()) {
            // Check freelancer services as fallback metadata
            if (freelancer.getServices() != null) {
                boolean hasServiceOverlap = freelancer.getServices().stream()
                        .anyMatch(s -> reqServiceNames.contains(s.getName().toLowerCase().trim())
                                || s.getName().toLowerCase().contains(eventType)
                                || eventType.contains(s.getName().toLowerCase()));
                if (hasServiceOverlap) {
                    return 0.35;
                }
            }
            return 0.0; // No portfolio or relevant service metadata
        }

        // 1. Exact or substring match on portfolio category name vs requirement eventType
        boolean categoryMatchesEvent = categories.stream().anyMatch(c -> {
            String cName = c.getName().toLowerCase().trim();
            return !eventType.isBlank() && (cName.contains(eventType) || eventType.contains(cName));
        });
        if (categoryMatchesEvent) {
            return 0.95;
        }

        // 2. Category name matches any required service name
        boolean categoryMatchesService = categories.stream().anyMatch(c -> {
            String cName = c.getName().toLowerCase().trim();
            return reqServiceNames.stream().anyMatch(s -> cName.contains(s) || s.contains(cName));
        });
        if (categoryMatchesService) {
            return 0.85;
        }

        // 3. Keyword match with creative brief / description
        boolean categoryMatchesDesc = categories.stream().anyMatch(c -> {
            String cName = c.getName().toLowerCase().trim();
            return !desc.isBlank() && desc.contains(cName);
        });
        if (categoryMatchesDesc) {
            return 0.70;
        }

        // 4. Freelancer has portfolio categories and photos in other genres
        long totalImages = categories.stream().mapToLong(c -> c.getImages() != null ? c.getImages().size() : 0).sum();
        if (totalImages > 0) {
            return 0.40;
        }

        return 0.20;
    }

    private double calculateExperience(Freelancer freelancer) {
        Integer exp = freelancer.getExperienceYears();
        if (exp == null || exp <= 0) {
            return 0.0;
        }
        return Math.min(1.0, exp / 10.0);
    }

    private double calculateBudgetCompatibility(WorkRequirement requirement, Freelancer freelancer) {
        BigDecimal budget = requirement.getBudget();
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.80;
        }

        BigDecimal rate = requirement.getDayType() == DayType.HALF_DAY
                ? freelancer.getHalfDayRate()
                : freelancer.getFullDayRate();

        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.50;
        }

        double b = budget.doubleValue();
        double r = rate.doubleValue();

        if (r <= b) {
            // Within budget: near 1.0, minor discount if significantly under budget
            double discountRatio = (b - r) / b;
            return Math.max(0.70, Math.min(1.0, 1.0 - 0.20 * discountRatio));
        } else {
            // Over budget: score drops linearly with excess percentage
            double excessRatio = (r - b) / b;
            return Math.max(0.0, 1.0 - excessRatio);
        }
    }

    private double calculateLocationDistance(WorkRequirement requirement, Freelancer freelancer) {
        Double distanceKm = calculateHaversineKm(
                requirement.getLatitude(), requirement.getLongitude(),
                freelancer.getLatitude(), freelancer.getLongitude());

        if (distanceKm != null) {
            // 0 km = 1.0, 50 km or greater = 0.0
            return Math.max(0.0, Math.min(1.0, 1.0 - (distanceKm / 50.0)));
        }

        // Fallback: Location string comparison
        String reqLoc = requirement.getLocation() != null ? requirement.getLocation().toLowerCase() : "";
        String frlAddr = freelancer.getAddress() != null ? freelancer.getAddress().toLowerCase() : "";

        if (!reqLoc.isBlank() && !frlAddr.isBlank()) {
            String[] majorCities = {"chennai", "coimbatore", "madurai", "bangalore", "bengaluru", "kochi", "salem", "trichy"};
            for (String city : majorCities) {
                if (reqLoc.contains(city) && frlAddr.contains(city)) {
                    return 0.80;
                }
            }
        }

        return 0.50; // Neutral default
    }

    private double calculateAvailabilityCompatibility(WorkRequirement requirement, Freelancer freelancer) {
        // Pre-checked by hard filter
        LocalDate date = requirement.getEventDate();
        if (date == null) {
            return 0.80;
        }

        LocalDate today = LocalDate.now();
        LocalDate windowEnd = today.plusDays(9);

        if (!date.isBefore(today) && !date.isAfter(windowEnd)) {
            // Within 10 days and passed hard filter -> verified available
            return 1.0;
        } else {
            // Beyond 10-day window -> availability unknown
            return 0.50;
        }
    }

    private Double calculateHaversineKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadiusKm * c;
        return Math.round(dist * 10.0) / 10.0;
    }

    private double round4(double val) {
        double bounded = Math.max(0.0, Math.min(1.0, val));
        return Math.round(bounded * 10000.0) / 10000.0;
    }

    private FreelancerCardDto mapToCardDto(Freelancer f, Double distanceKm, AvailabilityStatus slotStatus,
                                           String availableHours, Boolean withinWindow, String availabilityNotice) {
        FreelancerCardDto card = new FreelancerCardDto();
        card.setId(f.getId());
        card.setUserId(f.getUser() != null ? f.getUser().getId() : null);
        card.setFullName(f.getFullName());
        card.setProfilePhotoUrl(f.getProfilePhotoUrl());
        card.setPhone(f.getPhone());
        card.setAddress(f.getAddress());
        card.setLatitude(f.getLatitude());
        card.setLongitude(f.getLongitude());
        card.setDistanceKm(distanceKm);
        if (distanceKm != null) {
            card.setFormattedDistance(distanceKm + " km away");
        }
        card.setExperienceYears(f.getExperienceYears());
        card.setBio(f.getBio());
        card.setFullDayRate(f.getFullDayRate());
        card.setHalfDayRate(f.getHalfDayRate());

        // Ratings (Display only - strictly excluded from ML matching features)
        Long userId = f.getUser() != null ? f.getUser().getId() : null;
        Double avgRating = userId != null
                ? ratingRepository.findAverageScoreByToUserIdAndTargetType(userId, com.studiolynk.model.enums.RatingTargetType.FREELANCER)
                : 0.0;
        long reviewCount = userId != null
                ? ratingRepository.countByToUserIdAndTargetType(userId, com.studiolynk.model.enums.RatingTargetType.FREELANCER)
                : 0L;
        card.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        card.setReviewCount((int) reviewCount);

        // Skills
        if (f.getSkills() != null) {
            card.setSkills(f.getSkills().stream().map(s -> new SkillDto(s.getId(), s.getName(), s.isCustom())).toList());
        }

        // Services
        if (f.getServices() != null) {
            card.setServices(f.getServices().stream().map(s -> new ServiceDto(s.getId(), s.getName(), s.isCustom())).toList());
        }

        // Equipment
        if (f.getEquipment() != null) {
            card.setEquipment(f.getEquipment().stream().map(e -> new EquipmentDto(
                    e.getId(),
                    e.getCategory() != null ? e.getCategory().getId() : null,
                    e.getCategory() != null ? e.getCategory().getName() : null,
                    e.getName(),
                    e.isCustom())).toList());
        }

        // Primary role
        if (f.getServices() != null && !f.getServices().isEmpty()) {
            card.setPrimaryRole(f.getServices().iterator().next().getName());
        } else if (f.getSkills() != null && !f.getSkills().isEmpty()) {
            card.setPrimaryRole(f.getSkills().iterator().next().getName());
        } else {
            card.setPrimaryRole("Photographer / Cinematographer");
        }

        card.setAvailabilityStatus(slotStatus);
        card.setAvailableHours(availableHours);
        card.setWithinWindow(withinWindow);
        card.setAvailabilityNotice(availabilityNotice);
        card.setOnboardingCompleted(f.getUser() != null && f.getUser().isOnboardingCompleted());

        return card;
    }
}
