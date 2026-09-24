package com.studiolynk.service.impl;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.service.FreelancerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FreelancerServiceImpl implements FreelancerService {

    private final FreelancerRepository freelancerRepository;

    public FreelancerServiceImpl(FreelancerRepository freelancerRepository) {
        this.freelancerRepository = freelancerRepository;
    }

    @Override
    public Freelancer getFreelancerById(Long id) {
        return freelancerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + id));
    }

    @Override
    public Freelancer getFreelancerByUserId(Long userId) {
        return freelancerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found for user id: " + userId));
    }

    @Override
    public FreelancerSummaryDto getFreelancerSummary(Long id) {
        Freelancer freelancer = getFreelancerById(id);
        return mapToSummary(freelancer);
    }

    @Override
    public List<FreelancerSummaryDto> getAllFreelancers() {
        return freelancerRepository.findAll().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    private FreelancerSummaryDto mapToSummary(Freelancer freelancer) {
        FreelancerSummaryDto dto = new FreelancerSummaryDto();
        dto.setId(freelancer.getId());
        dto.setUserId(freelancer.getUser() != null ? freelancer.getUser().getId() : null);
        dto.setFullName(freelancer.getFullName());
        dto.setProfilePhotoUrl(freelancer.getProfilePhotoUrl());
        dto.setPhone(freelancer.getPhone());
        dto.setAddress(freelancer.getAddress());
        dto.setExperienceYears(freelancer.getExperienceYears());
        dto.setBio(freelancer.getBio());
        dto.setFullDayRate(freelancer.getFullDayRate());
        dto.setHalfDayRate(freelancer.getHalfDayRate());
        dto.setLatitude(freelancer.getLatitude());
        dto.setLongitude(freelancer.getLongitude());
        return dto;
    }
}
