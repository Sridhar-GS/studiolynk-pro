package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.DuplicateResourceException;
import com.studiolynk.exception.ForbiddenException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.RatingDto;
import com.studiolynk.model.dto.RatingSubmissionDto;
import com.studiolynk.model.dto.RatingSummaryDto;
import com.studiolynk.model.dto.RequirementRatingStatusDto;
import com.studiolynk.model.entity.*;
import com.studiolynk.model.enums.NotificationType;
import com.studiolynk.model.enums.RatingTargetType;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.*;
import com.studiolynk.service.NotificationService;
import com.studiolynk.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final RatingRepository ratingRepository;
    private final WorkRequirementRepository requirementRepository;
    private final StudioRepository studioRepository;
    private final FreelancerRepository freelancerRepository;
    private final NotificationService notificationService;

    public RatingServiceImpl(RatingRepository ratingRepository,
                             WorkRequirementRepository requirementRepository,
                             StudioRepository studioRepository,
                             FreelancerRepository freelancerRepository,
                             NotificationService notificationService) {
        this.ratingRepository = ratingRepository;
        this.requirementRepository = requirementRepository;
        this.studioRepository = studioRepository;
        this.freelancerRepository = freelancerRepository;
        this.notificationService = notificationService;
    }

    @Override
    public RatingDto submitRating(Long requirementId, User currentUser, RatingSubmissionDto dto) {
        WorkRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found: " + requirementId));

        // RAT-001, RAT-002, 12-RATING-SPECIFICATION: Only after work is COMPLETED
        if (requirement.getStatus() != RequirementStatus.COMPLETED) {
            throw new BadRequestException("Ratings are only permitted after work is completed (current status: "
                    + requirement.getStatus() + ")");
        }

        User studioUser = requirement.getStudio().getUser();
        Freelancer confirmedFreelancer = requirement.getConfirmedFreelancer();

        if (confirmedFreelancer == null) {
            throw new BadRequestException("Requirement does not have a confirmed freelancer to rate");
        }

        User freelancerUser = confirmedFreelancer.getUser();

        User toUser;
        RatingTargetType targetType;

        if (studioUser.getId().equals(currentUser.getId())) {
            // Studio rating Freelancer (RAT-001)
            toUser = freelancerUser;
            targetType = RatingTargetType.FREELANCER;
        } else if (freelancerUser.getId().equals(currentUser.getId())) {
            // Freelancer rating Studio (RAT-002)
            toUser = studioUser;
            targetType = RatingTargetType.STUDIO;
        } else {
            throw new ForbiddenException("Only the owning studio and confirmed freelancer can submit ratings for this work");
        }

        // RAT-006: A completed work shall permit one rating per direction
        if (ratingRepository.existsByRequirementIdAndFromUserId(requirementId, currentUser.getId())) {
            throw new DuplicateResourceException("You have already submitted a rating for this completed work (RAT-006)");
        }

        Rating rating = new Rating(
                requirement,
                currentUser,
                toUser,
                targetType,
                dto.getScore(),
                dto.getReviewText() != null ? dto.getReviewText().trim() : null
        );

        Rating saved = ratingRepository.save(rating);

        // Notify counterparty of the new rating & review
        try {
            String authorName = getDisplayNameForUser(currentUser);
            notificationService.createNotification(
                    toUser,
                    NotificationType.RATING_REMINDER,
                    "New Review Received: " + requirement.getEventName(),
                    authorName + " left you a " + saved.getScore() + "-star rating for " + requirement.getEventName(),
                    requirement.getId()
            );
        } catch (Exception e) {
            log.warn("Failed to dispatch rating notification: {}", e.getMessage());
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryDto getFreelancerRatings(Long freelancerId) {
        Freelancer freelancer = freelancerRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found: " + freelancerId));

        return buildSummaryDto(freelancer.getUser().getId(), RatingTargetType.FREELANCER);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryDto getStudioRatings(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new ResourceNotFoundException("Studio not found: " + studioId));

        return buildSummaryDto(studio.getUser().getId(), RatingTargetType.STUDIO);
    }

    @Override
    @Transactional(readOnly = true)
    public RequirementRatingStatusDto getRequirementRatingStatus(Long requirementId, User currentUser) {
        WorkRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found: " + requirementId));

        User studioUser = requirement.getStudio().getUser();
        Freelancer confirmedFreelancer = requirement.getConfirmedFreelancer();

        boolean isStudio = studioUser.getId().equals(currentUser.getId());
        boolean isFreelancer = confirmedFreelancer != null && confirmedFreelancer.getUser().getId().equals(currentUser.getId());

        if (!isStudio && !isFreelancer) {
            throw new ForbiddenException("You are not a participant in this requirement");
        }

        Optional<Rating> myRatingOpt = ratingRepository.findByRequirementIdAndFromUserId(requirementId, currentUser.getId());
        RatingDto myRating = myRatingOpt.map(this::mapToDto).orElse(null);

        Long counterpartyUserId = isStudio
                ? (confirmedFreelancer != null ? confirmedFreelancer.getUser().getId() : null)
                : studioUser.getId();

        Optional<Rating> counterpartyRatingOpt = counterpartyUserId != null
                ? ratingRepository.findByRequirementIdAndFromUserId(requirementId, counterpartyUserId)
                : Optional.empty();
        RatingDto counterpartyRating = counterpartyRatingOpt.map(this::mapToDto).orElse(null);

        boolean isCompleted = requirement.getStatus() == RequirementStatus.COMPLETED;
        boolean canRate = isCompleted && myRating == null && confirmedFreelancer != null;

        return new RequirementRatingStatusDto(
                requirement.getId(),
                requirement.getStatus().name(),
                canRate,
                myRating != null,
                myRating,
                counterpartyRating != null,
                counterpartyRating
        );
    }

    private RatingSummaryDto buildSummaryDto(Long toUserId, RatingTargetType targetType) {
        List<Rating> ratings = ratingRepository.findByToUserIdAndTargetTypeOrderByCreatedAtDesc(toUserId, targetType);
        long count = ratings.size();
        double avg = 0.0;
        if (count > 0) {
            double sum = 0.0;
            for (Rating r : ratings) {
                sum += r.getScore();
            }
            avg = Math.round((sum / count) * 10.0) / 10.0;
        }
        List<RatingDto> dtos = ratings.stream().map(this::mapToDto).collect(Collectors.toList());
        return new RatingSummaryDto(avg, count, dtos);
    }

    private RatingDto mapToDto(Rating rating) {
        String fromName = getDisplayNameForUser(rating.getFromUser());
        String toName = getDisplayNameForUser(rating.getToUser());

        return new RatingDto(
                rating.getId(),
                rating.getRequirement().getId(),
                rating.getRequirement().getEventName(),
                rating.getFromUser().getId(),
                fromName,
                rating.getFromUser().getRole().name(),
                rating.getToUser().getId(),
                toName,
                rating.getTargetType(),
                rating.getScore(),
                rating.getReviewText(),
                rating.getCreatedAt()
        );
    }

    private String getDisplayNameForUser(User user) {
        if (user == null) return "Unknown";
        if (user.getRole() == UserRole.STUDIO) {
            return studioRepository.findByUserId(user.getId())
                    .map(Studio::getStudioName)
                    .orElse(user.getEmail());
        } else if (user.getRole() == UserRole.FREELANCER) {
            return freelancerRepository.findByUserId(user.getId())
                    .map(Freelancer::getFullName)
                    .orElse(user.getEmail());
        }
        return user.getEmail();
    }
}
