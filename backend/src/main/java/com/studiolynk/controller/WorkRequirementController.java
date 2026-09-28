package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.RequirementStatusUpdateDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.dto.WorkRequirementResponseDto;
import com.studiolynk.model.dto.WorkRequirementSummaryDto;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.service.WorkRequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@Tag(name = "Work Requirements", description = "Studio work requirement creation, lifecycle, and discovery endpoints (WRK-001 - WRK-008)")
public class WorkRequirementController {

    private final WorkRequirementService requirementService;

    public WorkRequirementController(WorkRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping
    @Operation(summary = "Create a work/event requirement (WRK-001, WRK-002)")
    public ResponseEntity<ApiResponse<WorkRequirementResponseDto>> createRequirement(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkRequirementRequestDto request) {
        WorkRequirementResponseDto response = requirementService.createRequirement(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Work requirement created successfully.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing work requirement (WRK-001, WRK-002)")
    public ResponseEntity<ApiResponse<WorkRequirementResponseDto>> updateRequirement(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody WorkRequirementRequestDto request) {
        WorkRequirementResponseDto response = requirementService.updateRequirement(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Work requirement updated successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get requirement details by ID with role-based private contact masking (WRK-002, REQ-002, REQ-006)")
    public ResponseEntity<ApiResponse<WorkRequirementResponseDto>> getRequirementById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        WorkRequirementResponseDto response = requirementService.getRequirementById(id, email);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/studio/me")
    @Operation(summary = "Get all requirements created by the authenticated studio")
    public ResponseEntity<ApiResponse<List<WorkRequirementSummaryDto>>> getMyStudioRequirements(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) RequirementStatus status) {
        List<WorkRequirementSummaryDto> response = requirementService.getStudioRequirements(userDetails.getUsername(), status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update requirement lifecycle status (WRK-003)")
    public ResponseEntity<ApiResponse<WorkRequirementResponseDto>> updateRequirementStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RequirementStatusUpdateDto request) {
        WorkRequirementResponseDto response = requirementService.updateRequirementStatus(
                userDetails.getUsername(), id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Requirement status updated to " + request.getStatus() + ".", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an open or draft work requirement")
    public ResponseEntity<ApiResponse<Void>> deleteRequirement(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        requirementService.deleteRequirement(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Work requirement deleted successfully.", null));
    }

    @GetMapping("/open")
    @Operation(summary = "Public/freelancer feed of active open requirements (WRK-004)")
    public ResponseEntity<ApiResponse<List<WorkRequirementSummaryDto>>> getOpenRequirements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) String location) {
        List<WorkRequirementSummaryDto> response = requirementService.getOpenRequirementsForDiscovery(fromDate, location);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
