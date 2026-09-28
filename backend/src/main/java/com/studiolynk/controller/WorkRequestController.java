package com.studiolynk.controller;

import com.studiolynk.model.dto.AcceptRequestDto;
import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.CancelRequestDto;
import com.studiolynk.model.dto.CreateWorkRequestDto;
import com.studiolynk.model.dto.WorkRequestResponseDto;
import com.studiolynk.model.dto.WorkRequestSummaryDto;
import com.studiolynk.model.enums.RequestStatus;
import com.studiolynk.service.WorkRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Work Requests", description = "Endpoints for booking requests, negotiation, acceptance, confirmation, and cancellation (REQ-001 - REQ-009)")
public class WorkRequestController {

    private final WorkRequestService workRequestService;

    public WorkRequestController(WorkRequestService workRequestService) {
        this.workRequestService = workRequestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDIO')")
    @Operation(summary = "Send a work booking request to a freelancer (REQ-001, WRK-006)")
    public ResponseEntity<ApiResponse<WorkRequestResponseDto>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateWorkRequestDto dto
    ) {
        WorkRequestResponseDto response = workRequestService.createRequest(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Work request sent successfully.", response));
    }

    @GetMapping("/studio")
    @PreAuthorize("hasRole('STUDIO')")
    @Operation(summary = "Get all work requests sent by the authenticated studio")
    public ResponseEntity<ApiResponse<List<WorkRequestSummaryDto>>> getStudioRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) RequestStatus status
    ) {
        List<WorkRequestSummaryDto> response = workRequestService.getStudioRequests(
                userDetails.getUsername(), requirementId, status
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/freelancer")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Get all incoming work requests for the authenticated freelancer (REQ-002: client contact masked)")
    public ResponseEntity<ApiResponse<List<WorkRequestSummaryDto>>> getFreelancerRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) RequestStatus status
    ) {
        List<WorkRequestSummaryDto> response = workRequestService.getFreelancerRequests(
                userDetails.getUsername(), status
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a work request with dynamic privacy gating (REQ-002, REQ-006)")
    public ResponseEntity<ApiResponse<WorkRequestResponseDto>> getRequestById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        WorkRequestResponseDto response = workRequestService.getRequestById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Freelancer accepts a work request (REQ-004)")
    public ResponseEntity<ApiResponse<WorkRequestResponseDto>> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) AcceptRequestDto dto
    ) {
        WorkRequestResponseDto response = workRequestService.acceptRequest(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Work request accepted. Awaiting final studio confirmation.", response));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Freelancer declines a work request (REQ-004)")
    public ResponseEntity<ApiResponse<WorkRequestResponseDto>> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        WorkRequestResponseDto response = workRequestService.rejectRequest(userDetails.getUsername(), id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Work request declined.", response));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('STUDIO')")
    @Operation(summary = "Studio confirms the final freelancer (REQ-005, WRK-007, WRK-008: closes other candidates, REQ-006: reveals client contact)")
    public ResponseEntity<ApiResponse<WorkRequestResponseDto>> confirmRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        WorkRequestResponseDto response = workRequestService.confirmRequest(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer confirmed! Private client contact information is now unlocked.", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Studio or freelancer cancels confirmed work with a reason (REQ-007)")
    public ResponseEntity<ApiResponse<WorkRequestResponseDto>> cancelRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CancelRequestDto dto
    ) {
        WorkRequestResponseDto response = workRequestService.cancelRequest(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Work request cancelled.", response));
    }
}
