package com.studiolynk.service;

import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.model.entity.Freelancer;

import java.util.List;

public interface FreelancerService {
    Freelancer getFreelancerById(Long id);
    Freelancer getFreelancerByUserId(Long userId);
    FreelancerSummaryDto getFreelancerSummary(Long id);
    List<FreelancerSummaryDto> getAllFreelancers();
}
