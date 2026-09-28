package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.DuplicateResourceException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.AcceptRequestDto;
import com.studiolynk.model.dto.CancelRequestDto;
import com.studiolynk.model.dto.CreateWorkRequestDto;
import com.studiolynk.model.dto.EquipmentDto;
import com.studiolynk.model.dto.ServiceDto;
import com.studiolynk.model.dto.SkillDto;
import com.studiolynk.model.dto.WorkRequestResponseDto;
import com.studiolynk.model.dto.WorkRequestSummaryDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.entity.WorkRequest;
import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequestStatus;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.repository.WorkRequestRepository;
import com.studiolynk.repository.WorkRequirementRepository;
import com.studiolynk.service.WorkRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkRequestServiceImpl implements WorkRequestService {

    private static final Logger log = LoggerFactory.getLogger(WorkRequestServiceImpl.class);

    private final WorkRequestRepository workRequestRepository;
    private final WorkRequirementRepository workRequirementRepository;
    private final StudioRepository studioRepository;
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;

    public WorkRequestServiceImpl(
            WorkRequestRepository workRequestRepository,
            WorkRequirementRepository workRequirementRepository,
            StudioRepository studioRepository,
            FreelancerRepository freelancerRepository,
            UserRepository userRepository
    ) {
        this.workRequestRepository = workRequestRepository;
        this.workRequirementRepository = workRequirementRepository;
        this.studioRepository = studioRepository;
        this.freelancerRepository = freelancerRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public WorkRequestResponseDto createRequest(String studioEmail, CreateWorkRequestDto dto) {
        if (dto == null) {
            throw new BadRequestException("Request body cannot be null.");
        }

        Studio studio = getStudioByUserEmail(studioEmail);

        WorkRequirement requirement = workRequirementRepository.findById(dto.getRequirementId())
                .orElseThrow(() -> new ResourceNotFoundException("Work requirement not found with id: " + dto.getRequirementId()));

        if (!requirement.getStudio().getId().equals(studio.getId())) {
            throw new AccessDeniedException("You do not own this work requirement.");
        }

        if (requirement.getStatus() == RequirementStatus.COMPLETED || requirement.getStatus() == RequirementStatus.CANCELLED) {
            throw new BadRequestException("Cannot send requests for a " + requirement.getStatus() + " requirement.");
        }

        Freelancer freelancer = freelancerRepository.findById(dto.getFreelancerId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + dto.getFreelancerId()));

        if (freelancer.getUser() == null || !freelancer.getUser().isOnboardingCompleted()) {
            throw new BadRequestException("Cannot send request to a freelancer whose onboarding is incomplete.");
        }

        // Check for existing request
        workRequestRepository.findByRequirementIdAndFreelancerId(requirement.getId(), freelancer.getId())
                .ifPresent(existing -> {
                    if (existing.getStatus() == RequestStatus.PENDING ||
                            existing.getStatus() == RequestStatus.ACCEPTED ||
                            existing.getStatus() == RequestStatus.CONFIRMED) {
                        throw new DuplicateResourceException("A request has already been sent to this freelancer for this requirement.");
                    }
                });

        // REQ-009: Double booking prevention
        checkDoubleBooking(freelancer.getId(), requirement.getEventDate(), requirement.getStartTime(), requirement.getEndTime(), null);

        BigDecimal price = dto.getOfferedPrice() != null ? dto.getOfferedPrice() : requirement.getBudget();

        WorkRequest request = new WorkRequest(requirement, freelancer, price, dto.getMessage());
        WorkRequest saved = workRequestRepository.save(request);

        // Transition requirement to REQUESTED if OPEN
        if (requirement.getStatus() == RequirementStatus.OPEN || requirement.getStatus() == RequirementStatus.DRAFT) {
            requirement.setStatus(RequirementStatus.REQUESTED);
            workRequirementRepository.save(requirement);
        }

        log.info("Studio [{}] sent work request [{}] to freelancer [{}] for requirement [{}]",
                studio.getStudioName(), saved.getId(), freelancer.getFullName(), requirement.getEventName());

        return mapToResponseDto(saved, studioEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkRequestSummaryDto> getStudioRequests(String studioEmail, Long requirementId, RequestStatus status) {
        Studio studio = getStudioByUserEmail(studioEmail);

        List<WorkRequest> list;
        if (requirementId != null) {
            if (status != null) {
                list = workRequestRepository.findByRequirementIdAndStatus(requirementId, status);
            } else {
                list = workRequestRepository.findByRequirementId(requirementId);
            }
            // Ensure owned by studio
            list = list.stream()
                    .filter(r -> r.getRequirement().getStudio().getId().equals(studio.getId()))
                    .collect(Collectors.toList());
        } else {
            if (status != null) {
                list = workRequestRepository.findByRequirementStudioIdAndStatus(studio.getId(), status);
            } else {
                list = workRequestRepository.findByRequirementStudioId(studio.getId());
            }
        }

        return list.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkRequestSummaryDto> getFreelancerRequests(String freelancerEmail, RequestStatus status) {
        Freelancer freelancer = getFreelancerByUserEmail(freelancerEmail);

        List<WorkRequest> list;
        if (status != null) {
            list = workRequestRepository.findByFreelancerIdAndStatus(freelancer.getId(), status);
        } else {
            list = workRequestRepository.findByFreelancerId(freelancer.getId());
        }

        return list.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkRequestResponseDto getRequestById(String userEmail, Long requestId) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        verifyPartyAccess(request, userEmail);

        return mapToResponseDto(request, userEmail);
    }

    @Override
    @Transactional
    public WorkRequestResponseDto acceptRequest(String freelancerEmail, Long requestId, AcceptRequestDto dto) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        Freelancer freelancer = getFreelancerByUserEmail(freelancerEmail);
        if (!request.getFreelancer().getId().equals(freelancer.getId())) {
            throw new AccessDeniedException("You are not the assigned freelancer for this work request.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING requests can be accepted. Current status: " + request.getStatus());
        }

        // REQ-009: Double booking prevention check before accepting
        WorkRequirement requirement = request.getRequirement();
        checkDoubleBooking(freelancer.getId(), requirement.getEventDate(), requirement.getStartTime(), requirement.getEndTime(), null);

        request.setStatus(RequestStatus.ACCEPTED);
        if (dto != null && dto.getAgreedPrice() != null) {
            request.setAgreedPrice(dto.getAgreedPrice());
        }
        WorkRequest saved = workRequestRepository.save(request);

        // Update requirement status to ACCEPTED if currently REQUESTED
        if (requirement.getStatus() == RequirementStatus.REQUESTED) {
            requirement.setStatus(RequirementStatus.ACCEPTED);
            workRequirementRepository.save(requirement);
        }

        log.info("Freelancer [{}] accepted work request [{}]", freelancer.getFullName(), saved.getId());

        // Note: REQ-006: Private contact info is STILL masked because studio has not confirmed yet!
        return mapToResponseDto(saved, freelancerEmail);
    }

    @Override
    @Transactional
    public WorkRequestResponseDto rejectRequest(String freelancerEmail, Long requestId, String reason) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        Freelancer freelancer = getFreelancerByUserEmail(freelancerEmail);
        if (!request.getFreelancer().getId().equals(freelancer.getId())) {
            throw new AccessDeniedException("You are not the assigned freelancer for this work request.");
        }

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.ACCEPTED) {
            throw new BadRequestException("Cannot reject a request in status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setCancellationReason(reason != null && !reason.isBlank() ? reason : "Declined by freelancer.");
        WorkRequest saved = workRequestRepository.save(request);

        log.info("Freelancer [{}] rejected work request [{}]", freelancer.getFullName(), saved.getId());
        return mapToResponseDto(saved, freelancerEmail);
    }

    @Override
    @Transactional
    public WorkRequestResponseDto confirmRequest(String studioEmail, Long requestId) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        Studio studio = getStudioByUserEmail(studioEmail);
        WorkRequirement requirement = request.getRequirement();

        if (!requirement.getStudio().getId().equals(studio.getId())) {
            throw new AccessDeniedException("You do not own the requirement for this work request.");
        }

        if (request.getStatus() != RequestStatus.ACCEPTED && request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Cannot confirm a request in status: " + request.getStatus());
        }

        Freelancer freelancer = request.getFreelancer();

        // REQ-009: Strict double booking check before final confirmation
        checkDoubleBooking(freelancer.getId(), requirement.getEventDate(), requirement.getStartTime(), requirement.getEndTime(), requirement.getId());

        // REQ-005, WRK-007: Confirm this freelancer
        request.setStatus(RequestStatus.CONFIRMED);
        if (request.getAgreedPrice() == null) {
            request.setAgreedPrice(requirement.getBudget());
        }
        WorkRequest saved = workRequestRepository.save(request);

        requirement.setConfirmedFreelancer(freelancer);
        requirement.setStatus(RequirementStatus.CONFIRMED);
        workRequirementRepository.save(requirement);

        // WRK-008: Automatically close/reject other pending or accepted requests for this requirement
        List<WorkRequest> otherRequests = workRequestRepository.findByRequirementIdAndIdNot(requirement.getId(), requestId);
        int closedCount = 0;
        for (WorkRequest other : otherRequests) {
            if (other.getStatus() == RequestStatus.PENDING || other.getStatus() == RequestStatus.ACCEPTED) {
                other.setStatus(RequestStatus.REJECTED);
                other.setCancellationReason("Studio confirmed another creator for this assignment.");
                workRequestRepository.save(other);
                closedCount++;
            }
        }

        log.info("Studio [{}] confirmed work request [{}] with freelancer [{}]. Closed {} other pending requests.",
                studio.getStudioName(), saved.getId(), freelancer.getFullName(), closedCount);

        // Now that status is CONFIRMED, private client contact details will be revealed (REQ-006)
        return mapToResponseDto(saved, studioEmail);
    }

    @Override
    @Transactional
    public WorkRequestResponseDto cancelRequest(String userEmail, Long requestId, CancelRequestDto dto) {
        if (dto == null || dto.getReason() == null || dto.getReason().isBlank()) {
            throw new BadRequestException("A cancellation reason is required (REQ-007).");
        }

        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Work request not found with id: " + requestId));

        verifyPartyAccess(request, userEmail);

        if (request.getStatus() == RequestStatus.CANCELLED || request.getStatus() == RequestStatus.REJECTED) {
            throw new BadRequestException("Request is already " + request.getStatus() + ".");
        }

        boolean wasConfirmed = request.getStatus() == RequestStatus.CONFIRMED;
        request.setStatus(RequestStatus.CANCELLED);
        request.setCancellationReason(dto.getReason().trim());
        WorkRequest saved = workRequestRepository.save(request);

        // If requirement was confirmed with this freelancer, reset requirement status
        WorkRequirement requirement = request.getRequirement();
        if (wasConfirmed && requirement.getConfirmedFreelancer() != null &&
                requirement.getConfirmedFreelancer().getId().equals(request.getFreelancer().getId())) {
            requirement.setConfirmedFreelancer(null);
            requirement.setStatus(RequirementStatus.CANCELLED);
            workRequirementRepository.save(requirement);
        }

        log.info("Work request [{}] cancelled by user [{}]. Reason: {}", requestId, userEmail, dto.getReason());
        return mapToResponseDto(saved, userEmail);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private void verifyPartyAccess(WorkRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isStudioOwner = request.getRequirement().getStudio().getUser().getId().equals(user.getId());
        boolean isAssignedFreelancer = request.getFreelancer().getUser().getId().equals(user.getId());

        if (!isStudioOwner && !isAssignedFreelancer) {
            throw new AccessDeniedException("You are not a party to this work request.");
        }
    }

    private void checkDoubleBooking(Long freelancerId, java.time.LocalDate eventDate, LocalTime startTime, LocalTime endTime, Long currentRequirementId) {
        List<WorkRequest> confirmedRequests = workRequestRepository.findByFreelancerIdAndStatusAndEventDate(
                freelancerId, RequestStatus.CONFIRMED, eventDate
        );

        for (WorkRequest cr : confirmedRequests) {
            if (currentRequirementId != null && cr.getRequirement().getId().equals(currentRequirementId)) {
                continue;
            }
            WorkRequirement req = cr.getRequirement();
            if (isTimeOverlapping(startTime, endTime, req.getStartTime(), req.getEndTime())) {
                throw new BadRequestException("Freelancer already has a confirmed shoot on " + eventDate +
                        " from " + req.getStartTime() + " to " + req.getEndTime() + ".");
            }
        }
    }

    private boolean isTimeOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return true; // Overlap assumed if unspecified
        }
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private Studio getStudioByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return studioRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user: " + userEmail));
    }

    private Freelancer getFreelancerByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return freelancerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userEmail));
    }

    private WorkRequestResponseDto mapToResponseDto(WorkRequest request, String viewerEmail) {
        WorkRequestResponseDto dto = new WorkRequestResponseDto();
        dto.setId(request.getId());
        dto.setStatus(request.getStatus());
        dto.setAgreedPrice(request.getAgreedPrice());
        dto.setMessage(request.getMessage());
        dto.setCancellationReason(request.getCancellationReason());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        Freelancer fl = request.getFreelancer();
        if (fl != null) {
            dto.setFreelancerId(fl.getId());
            dto.setFreelancerName(fl.getFullName());
            dto.setFreelancerPhotoUrl(fl.getProfilePhotoUrl());
            dto.setFreelancerPhone(fl.getPhone());
        }

        WorkRequirement req = request.getRequirement();
        if (req != null) {
            dto.setRequirementId(req.getId());
            dto.setEventName(req.getEventName());
            dto.setEventType(req.getEventType());
            dto.setEventDate(req.getEventDate());
            dto.setStartTime(req.getStartTime());
            dto.setEndTime(req.getEndTime());
            dto.setFormattedTime(req.getStartTime() + " - " + req.getEndTime());
            dto.setLocation(req.getLocation());
            dto.setDayType(req.getDayType());
            dto.setBudget(req.getBudget());
            dto.setDescription(req.getDescription());

            Studio st = req.getStudio();
            if (st != null) {
                dto.setStudioId(st.getId());
                dto.setStudioName(st.getStudioName());
                dto.setStudioLogoUrl(st.getLogoUrl());
                dto.setStudioPhone(st.getPhone());
            }

            dto.setRequiredSkills(req.getRequiredSkills().stream()
                    .map(s -> new SkillDto(s.getId(), s.getName(), s.isCustom()))
                    .collect(Collectors.toList()));
            dto.setRequiredServices(req.getRequiredServices().stream()
                    .map(s -> new ServiceDto(s.getId(), s.getName(), s.isCustom()))
                    .collect(Collectors.toList()));
            dto.setRequiredEquipment(req.getRequiredEquipment().stream()
                    .map(e -> new EquipmentDto(
                            e.getId(),
                            e.getCategory() != null ? e.getCategory().getId() : null,
                            e.getCategory() != null ? e.getCategory().getName() : null,
                            e.getName(),
                            e.isCustom()
                    ))
                    .collect(Collectors.toList()));

            // REQ-002, REQ-006: Privacy gating for private client contact details
            boolean hasContact = req.getEventContactName() != null || req.getEventContactPhone() != null;
            dto.setHasPrivateContactDetails(hasContact);

            boolean isStudioOwner = st != null && st.getUser() != null && st.getUser().getEmail().equals(viewerEmail);
            boolean isConfirmedFreelancer = request.getStatus() == RequestStatus.CONFIRMED &&
                    fl != null && fl.getUser() != null && fl.getUser().getEmail().equals(viewerEmail);

            if (isStudioOwner || isConfirmedFreelancer) {
                // Reveal private details
                dto.setEventContactName(req.getEventContactName());
                dto.setEventContactPhone(req.getEventContactPhone());
                dto.setPrivateDetailsRevealed(true);
            } else {
                // Mask private details (REQ-002: Freelancer initially sees event details but not private client contact)
                dto.setEventContactName(null);
                dto.setEventContactPhone(null);
                dto.setPrivateDetailsRevealed(false);
            }
        }

        return dto;
    }

    private WorkRequestSummaryDto mapToSummaryDto(WorkRequest request) {
        WorkRequestSummaryDto dto = new WorkRequestSummaryDto();
        dto.setId(request.getId());
        dto.setStatus(request.getStatus());
        dto.setAgreedPrice(request.getAgreedPrice());
        dto.setMessage(request.getMessage());
        dto.setCancellationReason(request.getCancellationReason());
        dto.setCreatedAt(request.getCreatedAt());

        Freelancer fl = request.getFreelancer();
        if (fl != null) {
            dto.setFreelancerId(fl.getId());
            dto.setFreelancerName(fl.getFullName());
            dto.setFreelancerPhotoUrl(fl.getProfilePhotoUrl());
            dto.setFreelancerCity(fl.getAddress());
            if (fl.getSkills() != null && !fl.getSkills().isEmpty()) {
                dto.setFreelancerPrimarySkill(fl.getSkills().iterator().next().getName());
            }
        }

        WorkRequirement req = request.getRequirement();
        if (req != null) {
            dto.setRequirementId(req.getId());
            dto.setEventName(req.getEventName());
            dto.setEventType(req.getEventType());
            dto.setEventDate(req.getEventDate());
            dto.setStartTime(req.getStartTime());
            dto.setEndTime(req.getEndTime());
            dto.setFormattedTime(req.getStartTime() + " - " + req.getEndTime());
            dto.setLocation(req.getLocation());
            dto.setDayType(req.getDayType());
            dto.setBudget(req.getBudget());

            Studio st = req.getStudio();
            if (st != null) {
                dto.setStudioId(st.getId());
                dto.setStudioName(st.getStudioName());
                dto.setStudioLogoUrl(st.getLogoUrl());
            }
        }

        return dto;
    }
}
