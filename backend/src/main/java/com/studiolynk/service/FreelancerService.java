package com.studiolynk.service;

import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.FreelancerProfileDto;
import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.model.dto.FreelancerUpdateRequestDto;
import com.studiolynk.model.entity.Freelancer;

import java.util.List;

public interface FreelancerService {
    Freelancer getFreelancerById(Long id);
    Freelancer getFreelancerByUserId(Long userId);
    FreelancerSummaryDto getFreelancerSummary(Long id);
    List<FreelancerSummaryDto> getAllFreelancers();

    // Phase 5: Freelancer Onboarding and Profile Management
    FreelancerProfileDto saveOrUpdateOnboarding(String userEmail, FreelancerOnboardingRequestDto dto, boolean completeOnboarding);
    FreelancerProfileDto getFreelancerProfileByEmail(String userEmail);
    FreelancerProfileDto getFreelancerProfileById(Long freelancerId);
    FreelancerProfileDto updateFreelancerProfile(String userEmail, FreelancerUpdateRequestDto dto);
}
