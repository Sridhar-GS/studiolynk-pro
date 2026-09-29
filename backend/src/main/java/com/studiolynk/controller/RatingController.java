package com.studiolynk.controller;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.RatingDto;
import com.studiolynk.model.dto.RatingSubmissionDto;
import com.studiolynk.model.dto.RatingSummaryDto;
import com.studiolynk.model.dto.RequirementRatingStatusDto;
import com.studiolynk.model.entity.User;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Ratings", description = "Mutual star ratings and reviews for completed work (RAT-001 - RAT-006)")
public class RatingController {

    private final RatingService ratingService;
    private final UserRepository userRepository;

    public RatingController(RatingService ratingService, UserRepository userRepository) {
        this.ratingService = ratingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/requirements/{id}/ratings")
    @Operation(summary = "Submit rating for completed work", description = "Submit a 1-5 star score and optional review for completed requirement (RAT-001, RAT-002, RAT-005, RAT-006)")
    public ResponseEntity<ApiResponse<RatingDto>> submitRating(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RatingSubmissionDto dto
    ) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userDetails.getUsername()));

        RatingDto rating = ratingService.submitRating(id, currentUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Rating submitted successfully", rating));
    }

    @GetMapping("/api/requirements/{id}/ratings/status")
    @Operation(summary = "Get requirement rating status", description = "Get rating eligibility, current user rating, and counterparty rating for a requirement")
    public ResponseEntity<ApiResponse<RequirementRatingStatusDto>> getRequirementRatingStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userDetails.getUsername()));

        RequirementRatingStatusDto status = ratingService.getRequirementRatingStatus(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Requirement rating status retrieved", status));
    }

    @GetMapping("/api/freelancers/{id}/ratings")
    @Operation(summary = "Get public freelancer ratings", description = "Get public average rating, total count, and reviews for a freelancer (RAT-003)")
    public ResponseEntity<ApiResponse<RatingSummaryDto>> getFreelancerRatings(@PathVariable Long id) {
        RatingSummaryDto summary = ratingService.getFreelancerRatings(id);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer ratings retrieved successfully", summary));
    }

    @GetMapping("/api/studios/{id}/ratings")
    @Operation(summary = "Get public studio ratings", description = "Get public average rating, total count, and reviews for a studio (RAT-003)")
    public ResponseEntity<ApiResponse<RatingSummaryDto>> getStudioRatings(@PathVariable Long id) {
        RatingSummaryDto summary = ratingService.getStudioRatings(id);
        return ResponseEntity.ok(ApiResponse.ok("Studio ratings retrieved successfully", summary));
    }
}
