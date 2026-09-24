package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.FreelancerProfileDto;
import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.model.dto.FreelancerUpdateRequestDto;
import com.studiolynk.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/freelancers")
@Tag(name = "Freelancer", description = "Freelancer profile and search endpoints")
public class FreelancerController {

    private final FreelancerService freelancerService;

    public FreelancerController(FreelancerService freelancerService) {
        this.freelancerService = freelancerService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated freelancer's profile (FRL-006)")
    public ResponseEntity<ApiResponse<FreelancerProfileDto>> getCurrentFreelancerProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        FreelancerProfileDto profile = freelancerService.getFreelancerProfileByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current authenticated freelancer's profile (FRL-006)")
    public ResponseEntity<ApiResponse<FreelancerProfileDto>> updateCurrentFreelancerProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FreelancerUpdateRequestDto request) {
        FreelancerProfileDto updated = freelancerService.updateFreelancerProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer profile updated successfully.", updated));
    }

    @GetMapping
    @Operation(summary = "Get all registered freelancers")
    public ResponseEntity<ApiResponse<List<FreelancerSummaryDto>>> getAllFreelancers() {
        return ResponseEntity.ok(ApiResponse.ok(freelancerService.getAllFreelancers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get freelancer profile by ID")
    public ResponseEntity<ApiResponse<FreelancerProfileDto>> getFreelancerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(freelancerService.getFreelancerProfileById(id)));
    }
}
