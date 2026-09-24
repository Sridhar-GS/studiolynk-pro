package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.EquipmentDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.FreelancerProfileDto;
import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.model.dto.FreelancerUpdateRequestDto;
import com.studiolynk.model.dto.ServiceDto;
import com.studiolynk.model.dto.SkillDto;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.FreelancerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FreelancerServiceImpl implements FreelancerService {

    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final EquipmentRepository equipmentRepository;

    public FreelancerServiceImpl(
            FreelancerRepository freelancerRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            ServiceRepository serviceRepository,
            EquipmentRepository equipmentRepository) {
        this.freelancerRepository = freelancerRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Freelancer getFreelancerById(Long id) {
        return freelancerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Freelancer getFreelancerByUserId(Long userId) {
        return freelancerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user id: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public FreelancerSummaryDto getFreelancerSummary(Long id) {
        Freelancer freelancer = getFreelancerById(id);
        return mapToSummary(freelancer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FreelancerSummaryDto> getAllFreelancers() {
        return freelancerRepository.findAll().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public FreelancerProfileDto saveOrUpdateOnboarding(String userEmail, FreelancerOnboardingRequestDto dto, boolean completeOnboarding) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != UserRole.FREELANCER) {
            throw new BadRequestException("Only Freelancer accounts can submit freelancer onboarding. Account role is: " + user.getRole());
        }

        Freelancer freelancer = freelancerRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Freelancer newFreelancer = new Freelancer();
                    newFreelancer.setUser(user);
                    return newFreelancer;
                });

        // Core profile fields (FRL-001)
        freelancer.setFullName(dto.getFullName().trim());
        freelancer.setPhone(dto.getPhone().trim());
        freelancer.setAddress(dto.getAddress().trim());
        freelancer.setLatitude(dto.getLatitude());
        freelancer.setLongitude(dto.getLongitude());
        freelancer.setExperienceYears(dto.getExperienceYears() != null ? dto.getExperienceYears() : 0);
        freelancer.setBio(dto.getBio());
        freelancer.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        freelancer.setFullDayRate(dto.getFullDayRate() != null ? dto.getFullDayRate() : BigDecimal.ZERO);
        freelancer.setHalfDayRate(dto.getHalfDayRate() != null ? dto.getHalfDayRate() : BigDecimal.ZERO);

        // Associate Skills (FRL-002)
        if (dto.getSkillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(dto.getSkillIds()));
            freelancer.setSkills(skills);
        }

        // Associate Services (FRL-003)
        if (dto.getServiceIds() != null) {
            Set<ServiceEntity> services = new HashSet<>(serviceRepository.findAllById(dto.getServiceIds()));
            freelancer.setServices(services);
        }

        // Associate Equipment (FRL-004, FRL-005)
        if (dto.getEquipmentIds() != null) {
            Set<Equipment> equipment = new HashSet<>(equipmentRepository.findAllById(dto.getEquipmentIds()));
            freelancer.setEquipment(equipment);
        }

        Freelancer savedFreelancer = freelancerRepository.save(freelancer);

        // Finalize onboarding if completed (ONB-002, FRL-007: no admin verification required)
        if (completeOnboarding) {
            user.setOnboardingCompleted(true);
            userRepository.save(user);
        }

        return mapToProfile(savedFreelancer);
    }

    @Override
    @Transactional(readOnly = true)
    public FreelancerProfileDto getFreelancerProfileByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Freelancer freelancer = freelancerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userEmail));

        return mapToProfile(freelancer);
    }

    @Override
    @Transactional(readOnly = true)
    public FreelancerProfileDto getFreelancerProfileById(Long freelancerId) {
        Freelancer freelancer = getFreelancerById(freelancerId);
        return mapToProfile(freelancer);
    }

    @Override
    public FreelancerProfileDto updateFreelancerProfile(String userEmail, FreelancerUpdateRequestDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Freelancer freelancer = freelancerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userEmail));

        freelancer.setFullName(dto.getFullName().trim());
        freelancer.setPhone(dto.getPhone().trim());
        freelancer.setAddress(dto.getAddress().trim());
        freelancer.setLatitude(dto.getLatitude());
        freelancer.setLongitude(dto.getLongitude());
        freelancer.setExperienceYears(dto.getExperienceYears() != null ? dto.getExperienceYears() : 0);
        freelancer.setBio(dto.getBio());
        freelancer.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        freelancer.setFullDayRate(dto.getFullDayRate() != null ? dto.getFullDayRate() : BigDecimal.ZERO);
        freelancer.setHalfDayRate(dto.getHalfDayRate() != null ? dto.getHalfDayRate() : BigDecimal.ZERO);

        if (dto.getSkillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(dto.getSkillIds()));
            freelancer.setSkills(skills);
        }

        if (dto.getServiceIds() != null) {
            Set<ServiceEntity> services = new HashSet<>(serviceRepository.findAllById(dto.getServiceIds()));
            freelancer.setServices(services);
        }

        if (dto.getEquipmentIds() != null) {
            Set<Equipment> equipment = new HashSet<>(equipmentRepository.findAllById(dto.getEquipmentIds()));
            freelancer.setEquipment(equipment);
        }

        Freelancer savedFreelancer = freelancerRepository.save(freelancer);
        return mapToProfile(savedFreelancer);
    }

    private FreelancerProfileDto mapToProfile(Freelancer freelancer) {
        FreelancerProfileDto dto = new FreelancerProfileDto();
        dto.setId(freelancer.getId());
        dto.setUserId(freelancer.getUser() != null ? freelancer.getUser().getId() : null);
        dto.setEmail(freelancer.getUser() != null ? freelancer.getUser().getEmail() : null);
        dto.setFullName(freelancer.getFullName());
        dto.setProfilePhotoUrl(freelancer.getProfilePhotoUrl());
        dto.setPhone(freelancer.getPhone());
        dto.setAddress(freelancer.getAddress());
        dto.setLatitude(freelancer.getLatitude());
        dto.setLongitude(freelancer.getLongitude());
        dto.setExperienceYears(freelancer.getExperienceYears());
        dto.setBio(freelancer.getBio());
        dto.setFullDayRate(freelancer.getFullDayRate());
        dto.setHalfDayRate(freelancer.getHalfDayRate());
        dto.setOnboardingCompleted(freelancer.getUser() != null && freelancer.getUser().isOnboardingCompleted());
        dto.setCreatedAt(freelancer.getCreatedAt());
        dto.setUpdatedAt(freelancer.getUpdatedAt());

        // Skills
        if (freelancer.getSkills() != null) {
            List<SkillDto> skillDtos = freelancer.getSkills().stream()
                    .map(s -> new SkillDto(s.getId(), s.getName(), s.isCustom()))
                    .collect(Collectors.toList());
            dto.setSkills(skillDtos);
        }

        // Services
        if (freelancer.getServices() != null) {
            List<ServiceDto> serviceDtos = freelancer.getServices().stream()
                    .map(s -> new ServiceDto(s.getId(), s.getName(), s.isCustom()))
                    .collect(Collectors.toList());
            dto.setServices(serviceDtos);
        }

        // Equipment
        if (freelancer.getEquipment() != null) {
            List<EquipmentDto> equipmentDtos = freelancer.getEquipment().stream()
                    .map(e -> new EquipmentDto(
                            e.getId(),
                            e.getCategory() != null ? e.getCategory().getId() : null,
                            e.getCategory() != null ? e.getCategory().getName() : null,
                            e.getName(),
                            e.isCustom()))
                    .collect(Collectors.toList());
            dto.setEquipment(equipmentDtos);
        }

        dto.setCompletionPercentage(calculateCompletionPercentage(freelancer));

        return dto;
    }

    /**
     * Calculates deterministic profile completion percentage (ONB-005).
     */
    private int calculateCompletionPercentage(Freelancer freelancer) {
        int score = 0;

        if (freelancer.getFullName() != null && !freelancer.getFullName().isBlank()) score += 15;
        if (freelancer.getPhone() != null && !freelancer.getPhone().isBlank()) score += 15;
        if (freelancer.getAddress() != null && !freelancer.getAddress().isBlank()) score += 10;
        if (freelancer.getLatitude() != null && freelancer.getLongitude() != null) score += 10;
        if (freelancer.getExperienceYears() != null && freelancer.getExperienceYears() >= 0) score += 10;
        if (freelancer.getFullDayRate() != null && freelancer.getFullDayRate().compareTo(BigDecimal.ZERO) > 0
                && freelancer.getHalfDayRate() != null && freelancer.getHalfDayRate().compareTo(BigDecimal.ZERO) > 0) {
            score += 10;
        }
        if (freelancer.getSkills() != null && !freelancer.getSkills().isEmpty()) score += 10;
        if (freelancer.getServices() != null && !freelancer.getServices().isEmpty()) score += 10;
        if (freelancer.getEquipment() != null && !freelancer.getEquipment().isEmpty()) score += 5;
        if (freelancer.getBio() != null && !freelancer.getBio().isBlank()) score += 5;

        return Math.min(100, score);
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
