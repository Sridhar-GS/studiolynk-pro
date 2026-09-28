package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.EquipmentDto;
import com.studiolynk.model.dto.ServiceDto;
import com.studiolynk.model.dto.SkillDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.dto.WorkRequirementResponseDto;
import com.studiolynk.model.dto.WorkRequirementSummaryDto;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.repository.WorkRequirementRepository;
import com.studiolynk.service.WorkRequirementService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkRequirementServiceImpl implements WorkRequirementService {

    private final WorkRequirementRepository requirementRepository;
    private final StudioRepository studioRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final EquipmentRepository equipmentRepository;
    private final FreelancerRepository freelancerRepository;

    public WorkRequirementServiceImpl(
            WorkRequirementRepository requirementRepository,
            StudioRepository studioRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            ServiceRepository serviceRepository,
            EquipmentRepository equipmentRepository,
            FreelancerRepository freelancerRepository) {
        this.requirementRepository = requirementRepository;
        this.studioRepository = studioRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.equipmentRepository = equipmentRepository;
        this.freelancerRepository = freelancerRepository;
    }

    @Override
    public WorkRequirementResponseDto createRequirement(String userEmail, WorkRequirementRequestDto request) {
        Studio studio = getStudioByUserEmail(userEmail);

        // Validate shoot hours
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be strictly before end time.");
        }

        WorkRequirement requirement = new WorkRequirement();
        requirement.setStudio(studio);
        requirement.setEventName(request.getEventName().trim());
        requirement.setEventType(request.getEventType().trim());
        requirement.setEventDate(request.getEventDate());
        requirement.setStartTime(request.getStartTime());
        requirement.setEndTime(request.getEndTime());
        requirement.setLocation(request.getLocation().trim());
        requirement.setLatitude(request.getLatitude());
        requirement.setLongitude(request.getLongitude());
        requirement.setDayType(request.getDayType() != null ? request.getDayType() : DayType.FULL_DAY);
        requirement.setBudget(request.getBudget());
        requirement.setDescription(request.getDescription());
        requirement.setStatus(request.getStatus() != null ? request.getStatus() : RequirementStatus.OPEN);

        // Protected client contact details (WRK-002, REQ-002, REQ-006)
        if (request.getEventContactName() != null && !request.getEventContactName().isBlank()) {
            requirement.setEventContactName(request.getEventContactName().trim());
        }
        if (request.getEventContactPhone() != null && !request.getEventContactPhone().isBlank()) {
            requirement.setEventContactPhone(request.getEventContactPhone().trim());
        }

        // Skills
        if (request.getRequiredSkillIds() != null && !request.getRequiredSkillIds().isEmpty()) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(request.getRequiredSkillIds()));
            requirement.setRequiredSkills(skills);
        }

        // Services
        if (request.getRequiredServiceIds() != null && !request.getRequiredServiceIds().isEmpty()) {
            Set<ServiceEntity> services = new HashSet<>(serviceRepository.findAllById(request.getRequiredServiceIds()));
            requirement.setRequiredServices(services);
        }

        // Equipment
        if (request.getRequiredEquipmentIds() != null && !request.getRequiredEquipmentIds().isEmpty()) {
            Set<Equipment> equipment = new HashSet<>(equipmentRepository.findAllById(request.getRequiredEquipmentIds()));
            requirement.setRequiredEquipment(equipment);
        }

        WorkRequirement saved = requirementRepository.save(requirement);
        return mapToResponseDto(saved, true);
    }

    @Override
    public WorkRequirementResponseDto updateRequirement(String userEmail, Long requirementId, WorkRequirementRequestDto request) {
        WorkRequirement requirement = getEntityById(requirementId);
        Studio studio = getStudioByUserEmail(userEmail);

        if (!requirement.getStudio().getId().equals(studio.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this work requirement.");
        }

        if (requirement.getStatus() == RequirementStatus.COMPLETED || requirement.getStatus() == RequirementStatus.CANCELLED) {
            throw new BadRequestException("Cannot edit a requirement that is " + requirement.getStatus() + ".");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be strictly before end time.");
        }

        requirement.setEventName(request.getEventName().trim());
        requirement.setEventType(request.getEventType().trim());
        requirement.setEventDate(request.getEventDate());
        requirement.setStartTime(request.getStartTime());
        requirement.setEndTime(request.getEndTime());
        requirement.setLocation(request.getLocation().trim());
        requirement.setLatitude(request.getLatitude());
        requirement.setLongitude(request.getLongitude());
        requirement.setDayType(request.getDayType() != null ? request.getDayType() : DayType.FULL_DAY);
        requirement.setBudget(request.getBudget());
        requirement.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            requirement.setStatus(request.getStatus());
        }

        requirement.setEventContactName(request.getEventContactName() != null ? request.getEventContactName().trim() : null);
        requirement.setEventContactPhone(request.getEventContactPhone() != null ? request.getEventContactPhone().trim() : null);

        if (request.getRequiredSkillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(request.getRequiredSkillIds()));
            requirement.setRequiredSkills(skills);
        }

        if (request.getRequiredServiceIds() != null) {
            Set<ServiceEntity> services = new HashSet<>(serviceRepository.findAllById(request.getRequiredServiceIds()));
            requirement.setRequiredServices(services);
        }

        if (request.getRequiredEquipmentIds() != null) {
            Set<Equipment> equipment = new HashSet<>(equipmentRepository.findAllById(request.getRequiredEquipmentIds()));
            requirement.setRequiredEquipment(equipment);
        }

        WorkRequirement saved = requirementRepository.save(requirement);
        return mapToResponseDto(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkRequirementResponseDto getRequirementById(Long requirementId, String userEmail) {
        WorkRequirement requirement = getEntityById(requirementId);

        boolean revealPrivateDetails = false;
        if (userEmail != null) {
            // Check if viewer is the owning studio
            if (requirement.getStudio().getUser() != null && userEmail.equalsIgnoreCase(requirement.getStudio().getUser().getEmail())) {
                revealPrivateDetails = true;
            }
            // Check if viewer is the confirmed freelancer
            else if (requirement.getConfirmedFreelancer() != null
                    && requirement.getConfirmedFreelancer().getUser() != null
                    && userEmail.equalsIgnoreCase(requirement.getConfirmedFreelancer().getUser().getEmail())) {
                revealPrivateDetails = true;
            }
        }

        return mapToResponseDto(requirement, revealPrivateDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkRequirementSummaryDto> getStudioRequirements(String userEmail, RequirementStatus status) {
        Studio studio = getStudioByUserEmail(userEmail);
        List<WorkRequirement> list;
        if (status != null) {
            list = requirementRepository.findByStudioIdAndStatusOrderByCreatedAtDesc(studio.getId(), status);
        } else {
            list = requirementRepository.findByStudioIdOrderByCreatedAtDesc(studio.getId());
        }
        return list.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkRequirementSummaryDto> getOpenRequirementsForDiscovery(LocalDate fromDate, String location) {
        LocalDate searchDate = fromDate != null ? fromDate : LocalDate.now();
        List<WorkRequirement> list = requirementRepository.findActiveOpenRequirements(RequirementStatus.OPEN, searchDate);

        if (location != null && !location.isBlank()) {
            String locLower = location.toLowerCase().trim();
            list = list.stream()
                    .filter(r -> r.getLocation() != null && r.getLocation().toLowerCase().contains(locLower))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    @Override
    public WorkRequirementResponseDto updateRequirementStatus(String userEmail, Long requirementId, RequirementStatus newStatus) {
        WorkRequirement requirement = getEntityById(requirementId);
        Studio studio = getStudioByUserEmail(userEmail);

        if (!requirement.getStudio().getId().equals(studio.getId())) {
            throw new AccessDeniedException("You do not have permission to update this requirement's status.");
        }

        requirement.setStatus(newStatus);
        WorkRequirement saved = requirementRepository.save(requirement);
        return mapToResponseDto(saved, true);
    }

    @Override
    public void deleteRequirement(String userEmail, Long requirementId) {
        WorkRequirement requirement = getEntityById(requirementId);
        Studio studio = getStudioByUserEmail(userEmail);

        if (!requirement.getStudio().getId().equals(studio.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this work requirement.");
        }

        if (requirement.getStatus() == RequirementStatus.CONFIRMED || requirement.getStatus() == RequirementStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot delete requirement with confirmed freelancer. Please cancel instead.");
        }

        requirementRepository.delete(requirement);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkRequirement getEntityById(Long requirementId) {
        return requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Work requirement not found with ID: " + requirementId));
    }

    private Studio getStudioByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != UserRole.STUDIO && user.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only studio accounts can manage work requirements.");
        }

        return studioRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user: " + userEmail));
    }

    private WorkRequirementResponseDto mapToResponseDto(WorkRequirement r, boolean revealPrivateDetails) {
        WorkRequirementResponseDto dto = new WorkRequirementResponseDto();
        dto.setId(r.getId());
        dto.setStudioId(r.getStudio() != null ? r.getStudio().getId() : null);
        dto.setStudioName(r.getStudio() != null ? r.getStudio().getStudioName() : null);
        dto.setStudioLogoUrl(r.getStudio() != null ? r.getStudio().getLogoUrl() : null);
        dto.setStudioPhone(r.getStudio() != null ? r.getStudio().getPhone() : null);
        dto.setEventName(r.getEventName());
        dto.setEventType(r.getEventType());
        dto.setEventDate(r.getEventDate());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        if (r.getStartTime() != null && r.getEndTime() != null) {
            dto.setFormattedTime(r.getStartTime() + " - " + r.getEndTime());
        }
        dto.setLocation(r.getLocation());
        dto.setLatitude(r.getLatitude());
        dto.setLongitude(r.getLongitude());
        dto.setDayType(r.getDayType());
        dto.setBudget(r.getBudget());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());

        // Privacy gating for event contact details (WRK-002, REQ-002, REQ-006)
        boolean hasDetails = (r.getEventContactName() != null && !r.getEventContactName().isBlank())
                || (r.getEventContactPhone() != null && !r.getEventContactPhone().isBlank());
        dto.setHasPrivateContactDetails(hasDetails);
        dto.setPrivateDetailsRevealed(revealPrivateDetails);

        if (revealPrivateDetails) {
            dto.setEventContactName(r.getEventContactName());
            dto.setEventContactPhone(r.getEventContactPhone());
        } else {
            dto.setEventContactName(null);
            dto.setEventContactPhone(null);
        }

        if (r.getConfirmedFreelancer() != null) {
            dto.setConfirmedFreelancerId(r.getConfirmedFreelancer().getId());
            dto.setConfirmedFreelancerName(r.getConfirmedFreelancer().getFullName());
            dto.setConfirmedFreelancerPhotoUrl(r.getConfirmedFreelancer().getProfilePhotoUrl());
        }

        if (r.getRequiredSkills() != null) {
            dto.setRequiredSkills(r.getRequiredSkills().stream()
                    .map(s -> new SkillDto(s.getId(), s.getName(), s.isCustom())).toList());
        }
        if (r.getRequiredServices() != null) {
            dto.setRequiredServices(r.getRequiredServices().stream()
                    .map(s -> new ServiceDto(s.getId(), s.getName(), s.isCustom())).toList());
        }
        if (r.getRequiredEquipment() != null) {
            dto.setRequiredEquipment(r.getRequiredEquipment().stream()
                    .map(e -> new EquipmentDto(
                            e.getId(),
                            e.getCategory() != null ? e.getCategory().getId() : null,
                            e.getCategory() != null ? e.getCategory().getName() : null,
                            e.getName(),
                            e.isCustom())).toList());
        }

        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        return dto;
    }

    private WorkRequirementSummaryDto mapToSummaryDto(WorkRequirement r) {
        WorkRequirementSummaryDto dto = new WorkRequirementSummaryDto();
        dto.setId(r.getId());
        dto.setStudioId(r.getStudio() != null ? r.getStudio().getId() : null);
        dto.setStudioName(r.getStudio() != null ? r.getStudio().getStudioName() : null);
        dto.setStudioLogoUrl(r.getStudio() != null ? r.getStudio().getLogoUrl() : null);
        dto.setEventName(r.getEventName());
        dto.setEventType(r.getEventType());
        dto.setEventDate(r.getEventDate());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        if (r.getStartTime() != null && r.getEndTime() != null) {
            dto.setFormattedTime(r.getStartTime() + " - " + r.getEndTime());
        }
        dto.setLocation(r.getLocation());
        dto.setDayType(r.getDayType());
        dto.setBudget(r.getBudget());
        dto.setStatus(r.getStatus());
        dto.setSkillsCount(r.getRequiredSkills() != null ? r.getRequiredSkills().size() : 0);
        dto.setServicesCount(r.getRequiredServices() != null ? r.getRequiredServices().size() : 0);
        dto.setEquipmentCount(r.getRequiredEquipment() != null ? r.getRequiredEquipment().size() : 0);
        if (r.getConfirmedFreelancer() != null) {
            dto.setConfirmedFreelancerId(r.getConfirmedFreelancer().getId());
            dto.setConfirmedFreelancerName(r.getConfirmedFreelancer().getFullName());
        }
        return dto;
    }
}
