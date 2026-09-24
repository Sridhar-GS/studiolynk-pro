package com.studiolynk.controller;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.OnboardingStatusDto;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing onboarding status inspection and routing guidance.
 */
@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "Onboarding gate and account completion status")
public class OnboardingController {

    private final UserRepository userRepository;

    public OnboardingController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
