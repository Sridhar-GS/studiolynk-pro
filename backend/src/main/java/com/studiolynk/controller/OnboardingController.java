package com.studiolynk.controller;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.FreelancerProfileDto;
import com.studiolynk.model.dto.OnboardingStatusDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.StudioProfileDto;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.FreelancerService;
import com.studiolynk.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing onboarding status inspection, draft persistence, and role-specific completion.
 */
@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "Onboarding gate, draft saving, and account completion workflows")
public class OnboardingController {

    private final UserRepository userRepository;
    private final StudioService studioService;
    private final FreelancerService freelancerService;

    public OnboardingController(UserRepository userRepository, StudioService studioService, FreelancerService freelancerService) {
        this.userRepository = userRepository;
        this.studioService = studioService;
        this.freelancerService = freelancerService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get current onboarding completion status and recommended redirect route")
    public ResponseEntity<ApiResponse<OnboardingStatusDto>> getOnboardingStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userDetails.getUsername()));

        String redirectUrl;
        if (user.getRole() == UserRole.ADMIN) {
            redirectUrl = "/admin/dashboard";
        } else if (user.isOnboardingCompleted()) {
            redirectUrl = user.getRole() == UserRole.STUDIO ? "/studio/dashboard" : "/freelancer/dashboard";
        } else {
            redirectUrl = user.getRole() == UserRole.STUDIO ? "/onboarding/studio" : "/onboarding/freelancer";
        }

        OnboardingStatusDto statusDto = new OnboardingStatusDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isOnboardingCompleted(),
                redirectUrl
        );

        return ResponseEntity.ok(ApiResponse.ok(statusDto));
    }

    @PostMapping("/studio")
    @Operation(summary = "Submit final studio onboarding and activate platform access (STU-001, STU-004)")
    public ResponseEntity<ApiResponse<StudioProfileDto>> completeStudioOnboarding(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudioOnboardingRequestDto request) {

        StudioProfileDto profile = studioService.saveOrUpdateOnboarding(userDetails.getUsername(), request, true);
        return ResponseEntity.ok(ApiResponse.ok("Studio onboarding completed successfully. Platform unlocked.", profile));
    }

    @PutMapping("/studio")
    @Operation(summary = "Save studio onboarding draft without completing (ONB-004)")
    public ResponseEntity<ApiResponse<StudioProfileDto>> saveStudioOnboardingDraft(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudioOnboardingRequestDto request) {

        StudioProfileDto profile = studioService.saveOrUpdateOnboarding(userDetails.getUsername(), request, false);
        return ResponseEntity.ok(ApiResponse.ok("Studio onboarding draft saved successfully.", profile));
    }

    @PostMapping("/freelancer")
    @Operation(summary = "Submit final freelancer onboarding and activate platform access (ONB-002, ONB-003, FRL-007)")
    public ResponseEntity<ApiResponse<FreelancerProfileDto>> completeFreelancerOnboarding(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FreelancerOnboardingRequestDto request) {

        FreelancerProfileDto profile = freelancerService.saveOrUpdateOnboarding(userDetails.getUsername(), request, true);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer onboarding completed successfully. Platform unlocked.", profile));
    }

    @PutMapping("/freelancer")
    @Operation(summary = "Save freelancer onboarding draft without completing (ONB-004)")
    public ResponseEntity<ApiResponse<FreelancerProfileDto>> saveFreelancerOnboardingDraft(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FreelancerOnboardingRequestDto request) {

        FreelancerProfileDto profile = freelancerService.saveOrUpdateOnboarding(userDetails.getUsername(), request, false);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer onboarding draft saved successfully.", profile));
    }
}
