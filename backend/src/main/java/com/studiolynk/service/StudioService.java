package com.studiolynk.service;

import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.StudioProfileDto;
import com.studiolynk.model.dto.StudioSummaryDto;
import com.studiolynk.model.dto.StudioUpdateRequestDto;
import com.studiolynk.model.entity.Studio;

import java.util.List;

public interface StudioService {
    Studio getStudioById(Long id);
    Studio getStudioByUserId(Long userId);
    StudioSummaryDto getStudioSummary(Long id);
    List<StudioSummaryDto> getAllStudios();

    // Phase 4: Studio Onboarding and Profile Management
    StudioProfileDto saveOrUpdateOnboarding(String userEmail, StudioOnboardingRequestDto dto, boolean completeOnboarding);
    StudioProfileDto getStudioProfileByEmail(String userEmail);
    StudioProfileDto getStudioProfileById(Long studioId);
    StudioProfileDto updateStudioProfile(String userEmail, StudioUpdateRequestDto dto);
}
