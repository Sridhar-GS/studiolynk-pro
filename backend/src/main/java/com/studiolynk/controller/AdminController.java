package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.admin.AdminActivityItemDto;
import com.studiolynk.model.dto.admin.AdminDashboardMetricsDto;
import com.studiolynk.model.dto.admin.AdminFreelancerItemDto;
import com.studiolynk.model.dto.admin.AdminRequestItemDto;
import com.studiolynk.model.dto.admin.AdminRequirementItemDto;
import com.studiolynk.model.dto.admin.AdminStudioIdentityDto;
import com.studiolynk.model.dto.admin.AdminStudioItemDto;
import com.studiolynk.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Admin platform monitoring operations (ADM-001 - ADM-006).
 * Restricted strictly to users with ROLE_ADMIN.
 * Strictly view-only / monitoring: zero edit, delete, or mutation endpoints (ADM-003, ADM-004).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Platform", description = "View-only monitoring and audit operations for StudioLynk administrators")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregated platform metrics and health indicators (ADM-001, ADM-003)")
    public ResponseEntity<ApiResponse<AdminDashboardMetricsDto>> getDashboardMetrics() {
        return ResponseEntity.ok(ApiResponse.ok("Admin dashboard metrics retrieved successfully.", adminService.getDashboardMetrics()));
    }

    @GetMapping("/studios")
    @Operation(summary = "Get view-only directory of all photography studios (ADM-003, ADM-005)")
    public ResponseEntity<ApiResponse<List<AdminStudioItemDto>>> getAllStudios() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStudios()));
    }

    @GetMapping("/freelancers")
    @Operation(summary = "Get view-only directory of all freelancers and creators (ADM-003)")
    public ResponseEntity<ApiResponse<List<AdminFreelancerItemDto>>> getAllFreelancers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllFreelancers()));
    }

    @GetMapping("/requirements")
    @Operation(summary = "Get view-only list of all work requirements (ADM-003)")
    public ResponseEntity<ApiResponse<List<AdminRequirementItemDto>>> getAllRequirements() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRequirements()));
    }

    @GetMapping("/requests")
    @Operation(summary = "Get view-only list of all work requests and proposals (ADM-003)")
    public ResponseEntity<ApiResponse<List<AdminRequestItemDto>>> getAllRequests() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRequests()));
    }

    @GetMapping("/activity")
    @Operation(summary = "Get unified platform activity event log for audit monitoring (ADM-003)")
    public ResponseEntity<ApiResponse<List<AdminActivityItemDto>>> getActivity(
            @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getPlatformActivity(limit)));
    }

    @GetMapping("/studios/{studioId}/identity")
    @Operation(summary = "View studio identity submission declaration and document status (ADM-005)")
    public ResponseEntity<ApiResponse<AdminStudioIdentityDto>> getStudioIdentity(@PathVariable Long studioId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStudioIdentity(studioId)));
    }

    @GetMapping("/identity-submissions")
    @Operation(summary = "View all studio identity submissions for compliance monitoring (ADM-005)")
    public ResponseEntity<ApiResponse<List<AdminStudioIdentityDto>>> getAllIdentitySubmissions() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStudioIdentities()));
    }
}
