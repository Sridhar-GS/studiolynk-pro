package com.studiolynk.service;

import com.studiolynk.model.dto.admin.AdminActivityItemDto;
import com.studiolynk.model.dto.admin.AdminDashboardMetricsDto;
import com.studiolynk.model.dto.admin.AdminFreelancerItemDto;
import com.studiolynk.model.dto.admin.AdminRequestItemDto;
import com.studiolynk.model.dto.admin.AdminRequirementItemDto;
import com.studiolynk.model.dto.admin.AdminStudioIdentityDto;
import com.studiolynk.model.dto.admin.AdminStudioItemDto;

import java.util.List;

/**
 * Service interface for Admin platform monitoring operations (ADM-001 - ADM-006).
 * In accordance with ADM-003 and ADM-004, this service strictly exposes read-only queries.
 * No data mutation, edit, delete, or user approval methods exist.
 */
public interface AdminService {

    /**
     * Aggregated KPI counters and platform health metrics (ADM-001, ADM-003).
     */
    AdminDashboardMetricsDto getDashboardMetrics();

    /**
     * View-only directory of all photography studios (ADM-003, ADM-005).
     */
    List<AdminStudioItemDto> getAllStudios();

    /**
     * View-only directory of all freelancers and creators (ADM-003).
     */
    List<AdminFreelancerItemDto> getAllFreelancers();

    /**
     * View-only list of all work requirements (ADM-003).
     */
    List<AdminRequirementItemDto> getAllRequirements();

    /**
     * View-only list of all work requests and booking proposals (ADM-003).
     */
    List<AdminRequestItemDto> getAllRequests();

    /**
     * Unified platform activity stream for audit monitoring (ADM-003).
     */
    List<AdminActivityItemDto> getPlatformActivity(int limit);

    /**
     * View-only list of studio identity declarations for monitoring compliance (ADM-005).
     */
    List<AdminStudioIdentityDto> getAllStudioIdentities();

    /**
     * View-only identity declaration for a specific studio (ADM-005).
     */
    AdminStudioIdentityDto getStudioIdentity(Long studioId);
}
