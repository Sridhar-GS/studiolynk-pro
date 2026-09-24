package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.StudioIdentitySubmissionDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.StudioProfileDto;
import com.studiolynk.model.dto.StudioSocialLinkDto;
import com.studiolynk.model.dto.StudioSummaryDto;
import com.studiolynk.model.dto.StudioUpdateRequestDto;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.model.entity.StudioIdentitySubmission;
import com.studiolynk.model.entity.StudioSocialLink;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.SubmissionStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.StudioIdentitySubmissionRepository;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.StudioSocialLinkRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.StudioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudioServiceImpl implements StudioService {

    private final StudioRepository studioRepository;
    private final UserRepository userRepository;
    private final StudioSocialLinkRepository socialLinkRepository;
    private final StudioIdentitySubmissionRepository identitySubmissionRepository;

    public StudioServiceImpl(StudioRepository studioRepository,
                             UserRepository userRepository,
                             StudioSocialLinkRepository socialLinkRepository,
                             StudioIdentitySubmissionRepository identitySubmissionRepository) {
        this.studioRepository = studioRepository;
        this.userRepository = userRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.identitySubmissionRepository = identitySubmissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Studio getStudioById(Long id) {
        return studioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Studio not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Studio getStudioByUserId(Long userId) {
        return studioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user id: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public StudioSummaryDto getStudioSummary(Long id) {
        Studio studio = getStudioById(id);
        return mapToSummary(studio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudioSummaryDto> getAllStudios() {
        return studioRepository.findAll().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public StudioProfileDto saveOrUpdateOnboarding(String userEmail, StudioOnboardingRequestDto dto, boolean completeOnboarding) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != UserRole.STUDIO) {
            throw new BadRequestException("Only Studio accounts can submit studio onboarding. Account role is: " + user.getRole());
        }

        Studio studio = studioRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Studio newStudio = new Studio();
                    newStudio.setUser(user);
                    return newStudio;
                });

        // Set core studio details (STU-001)
        studio.setStudioName(dto.getStudioName().trim());
        studio.setOwnerName(dto.getOwnerName().trim());
        studio.setPhone(dto.getPhone().trim());
        studio.setAddress(dto.getAddress().trim());
        studio.setLatitude(dto.getLatitude());
        studio.setLongitude(dto.getLongitude());
        studio.setYearsOfOperation(dto.getYearsOfOperation() != null ? dto.getYearsOfOperation() : 0);
        studio.setLogoUrl(dto.getLogoUrl());

        Studio savedStudio = studioRepository.save(studio);

        // Update social links
        if (dto.getSocialLinks() != null) {
            savedStudio.getSocialLinks().clear();
            for (StudioSocialLinkDto linkDto : dto.getSocialLinks()) {
                if (linkDto.getPlatformName() != null && !linkDto.getPlatformName().isBlank()
                        && linkDto.getUrl() != null && !linkDto.getUrl().isBlank()) {
                    StudioSocialLink link = new StudioSocialLink(savedStudio, linkDto.getPlatformName().trim(), linkDto.getUrl().trim());
                    savedStudio.getSocialLinks().add(link);
                }
            }
        }

        // STU-002: Identity declaration and prototype submission workflow
        if (dto.getDeclarationText() != null && !dto.getDeclarationText().isBlank()) {
            String docType = (dto.getDocumentType() != null && !dto.getDocumentType().isBlank())
                    ? dto.getDocumentType().trim()
                    : "OWNER_DECLARATION";

            // Under STU-004, no admin approval is required; automatically marks verified prototype submission
            StudioIdentitySubmission submission = new StudioIdentitySubmission(
                    savedStudio,
                    docType,
                    dto.getDocumentUrl(),
                    dto.getDeclarationText().trim(),
                    SubmissionStatus.VERIFIED
            );
            savedStudio.getIdentitySubmissions().add(submission);
        }

        savedStudio = studioRepository.save(savedStudio);

        // STU-004 / ONB-002: Mark user onboarding complete if submitted as final
        if (completeOnboarding) {
            user.setOnboardingCompleted(true);
            userRepository.save(user);
        }

        return mapToProfile(savedStudio);
    }

    @Override
    @Transactional(readOnly = true)
    public StudioProfileDto getStudioProfileByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Studio studio = studioRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user: " + userEmail));

        return mapToProfile(studio);
    }

    @Override
    @Transactional(readOnly = true)
    public StudioProfileDto getStudioProfileById(Long studioId) {
        Studio studio = getStudioById(studioId);
        return mapToProfile(studio);
    }

    @Override
    public StudioProfileDto updateStudioProfile(String userEmail, StudioUpdateRequestDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Studio studio = studioRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Studio profile not found for user: " + userEmail));

        studio.setStudioName(dto.getStudioName().trim());
        studio.setOwnerName(dto.getOwnerName().trim());
        studio.setPhone(dto.getPhone().trim());
        studio.setAddress(dto.getAddress().trim());
        studio.setLatitude(dto.getLatitude());
        studio.setLongitude(dto.getLongitude());
        studio.setYearsOfOperation(dto.getYearsOfOperation() != null ? dto.getYearsOfOperation() : 0);
        studio.setLogoUrl(dto.getLogoUrl());

        if (dto.getSocialLinks() != null) {
            studio.getSocialLinks().clear();
            for (StudioSocialLinkDto linkDto : dto.getSocialLinks()) {
                if (linkDto.getPlatformName() != null && !linkDto.getPlatformName().isBlank()
                        && linkDto.getUrl() != null && !linkDto.getUrl().isBlank()) {
                    StudioSocialLink link = new StudioSocialLink(studio, linkDto.getPlatformName().trim(), linkDto.getUrl().trim());
                    studio.getSocialLinks().add(link);
                }
            }
        }

        Studio savedStudio = studioRepository.save(studio);
        return mapToProfile(savedStudio);
    }

    private StudioProfileDto mapToProfile(Studio studio) {
        StudioProfileDto dto = new StudioProfileDto();
        dto.setId(studio.getId());
        dto.setUserId(studio.getUser() != null ? studio.getUser().getId() : null);
        dto.setEmail(studio.getUser() != null ? studio.getUser().getEmail() : null);
        dto.setStudioName(studio.getStudioName());
        dto.setOwnerName(studio.getOwnerName());
        dto.setLogoUrl(studio.getLogoUrl());
        dto.setPhone(studio.getPhone());
        dto.setAddress(studio.getAddress());
        dto.setLatitude(studio.getLatitude());
        dto.setLongitude(studio.getLongitude());
        dto.setYearsOfOperation(studio.getYearsOfOperation());
        dto.setOnboardingCompleted(studio.getUser() != null && studio.getUser().isOnboardingCompleted());
        dto.setCreatedAt(studio.getCreatedAt());
        dto.setUpdatedAt(studio.getUpdatedAt());

        // Social Links
        if (studio.getSocialLinks() != null) {
            List<StudioSocialLinkDto> linkDtos = studio.getSocialLinks().stream()
                    .map(l -> new StudioSocialLinkDto(l.getId(), l.getPlatformName(), l.getUrl()))
                    .collect(Collectors.toList());
            dto.setSocialLinks(linkDtos);
        }

        // Identity Submission
        if (studio.getIdentitySubmissions() != null && !studio.getIdentitySubmissions().isEmpty()) {
            // Pick most recent submission
            StudioIdentitySubmission latest = studio.getIdentitySubmissions().get(studio.getIdentitySubmissions().size() - 1);
            dto.setIdentitySubmission(new StudioIdentitySubmissionDto(
                    latest.getId(),
                    latest.getDocumentType(),
                    latest.getDocumentUrl(),
                    latest.getDeclarationText(),
                    latest.getStatus(),
                    latest.getSubmittedAt()
            ));
        }

        // Calculate Profile Completion Percentage (ONB-005)
        dto.setCompletionPercentage(calculateCompletionPercentage(studio));

        return dto;
    }

    /**
     * Calculates deterministic profile completion percentage (ONB-005).
     */
    private int calculateCompletionPercentage(Studio studio) {
        int percentage = 0;

        if (studio.getStudioName() != null && !studio.getStudioName().isBlank()) percentage += 15;
        if (studio.getOwnerName() != null && !studio.getOwnerName().isBlank()) percentage += 15;
        if (studio.getPhone() != null && !studio.getPhone().isBlank()) percentage += 15;
        if (studio.getAddress() != null && !studio.getAddress().isBlank()) percentage += 15;
        if (studio.getLatitude() != null && studio.getLongitude() != null) percentage += 10;
        if (studio.getYearsOfOperation() != null && studio.getYearsOfOperation() > 0) percentage += 10;
        if (studio.getLogoUrl() != null && !studio.getLogoUrl().isBlank()) percentage += 10;
        if (studio.getSocialLinks() != null && !studio.getSocialLinks().isEmpty()) percentage += 5;
        if (studio.getIdentitySubmissions() != null && !studio.getIdentitySubmissions().isEmpty()) percentage += 10;

        return Math.min(100, percentage);
    }

    private StudioSummaryDto mapToSummary(Studio studio) {
        StudioSummaryDto dto = new StudioSummaryDto();
        dto.setId(studio.getId());
        dto.setUserId(studio.getUser() != null ? studio.getUser().getId() : null);
        dto.setStudioName(studio.getStudioName());
        dto.setOwnerName(studio.getOwnerName());
        dto.setLogoUrl(studio.getLogoUrl());
        dto.setPhone(studio.getPhone());
        dto.setAddress(studio.getAddress());
        dto.setYearsOfOperation(studio.getYearsOfOperation());
        dto.setLatitude(studio.getLatitude());
        dto.setLongitude(studio.getLongitude());
        return dto;
    }
}
