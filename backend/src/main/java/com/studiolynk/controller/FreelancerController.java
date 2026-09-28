package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.FreelancerCardDto;
import com.studiolynk.model.dto.FreelancerProfileDto;
import com.studiolynk.model.dto.FreelancerSearchFilterDto;
import com.studiolynk.model.dto.FreelancerSearchResponseDto;
import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.model.dto.FreelancerUpdateRequestDto;
import com.studiolynk.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @GetMapping("/search")
    @Operation(summary = "Search freelancers with query filters (DIS-001, DIS-003)")
    public ResponseEntity<ApiResponse<FreelancerSearchResponseDto>> searchFreelancersGet(
            FreelancerSearchFilterDto filters) {
        FreelancerSearchResponseDto response = freelancerService.searchFreelancers(filters);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/search")
    @Operation(summary = "Search freelancers with filter request body (DIS-001, DIS-003)")
    public ResponseEntity<ApiResponse<FreelancerSearchResponseDto>> searchFreelancersPost(
            @RequestBody(required = false) FreelancerSearchFilterDto filters) {
        FreelancerSearchResponseDto response = freelancerService.searchFreelancers(filters != null ? filters : new FreelancerSearchFilterDto());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/card")
    @Operation(summary = "Get freelancer card details with live availability check (DIS-004, DIS-005)")
    public ResponseEntity<ApiResponse<FreelancerCardDto>> getFreelancerCardById(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        FreelancerCardDto card = freelancerService.getFreelancerCardById(id, date, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.ok(card));
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
