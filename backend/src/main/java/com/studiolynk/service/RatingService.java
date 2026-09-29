package com.studiolynk.service;

import com.studiolynk.model.dto.RatingDto;
import com.studiolynk.model.dto.RatingSubmissionDto;
import com.studiolynk.model.dto.RatingSummaryDto;
import com.studiolynk.model.dto.RequirementRatingStatusDto;
import com.studiolynk.model.entity.User;

public interface RatingService {

    RatingDto submitRating(Long requirementId, User currentUser, RatingSubmissionDto dto);

    RatingSummaryDto getFreelancerRatings(Long freelancerId);

    RatingSummaryDto getStudioRatings(Long studioId);

    RequirementRatingStatusDto getRequirementRatingStatus(Long requirementId, User currentUser);
}
