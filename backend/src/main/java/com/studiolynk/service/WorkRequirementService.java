package com.studiolynk.service;

import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.dto.WorkRequirementResponseDto;
import com.studiolynk.model.dto.WorkRequirementSummaryDto;
import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.enums.RequirementStatus;

import java.time.LocalDate;
import java.util.List;

public interface WorkRequirementService {

    WorkRequirementResponseDto createRequirement(String userEmail, WorkRequirementRequestDto request);

    WorkRequirementResponseDto updateRequirement(String userEmail, Long requirementId, WorkRequirementRequestDto request);

    WorkRequirementResponseDto getRequirementById(Long requirementId, String userEmail);

    List<WorkRequirementSummaryDto> getStudioRequirements(String userEmail, RequirementStatus status);

    List<WorkRequirementSummaryDto> getOpenRequirementsForDiscovery(LocalDate fromDate, String location);

    WorkRequirementResponseDto updateRequirementStatus(String userEmail, Long requirementId, RequirementStatus newStatus);

    void deleteRequirement(String userEmail, Long requirementId);

    WorkRequirement getEntityById(Long requirementId);
}
