package com.studiolynk.service.impl;

import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.StudioSummaryDto;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.service.StudioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudioServiceImpl implements StudioService {

    private final StudioRepository studioRepository;

    public StudioServiceImpl(StudioRepository studioRepository) {
        this.studioRepository = studioRepository;
    }

    @Override
    public Studio getStudioById(Long id) {
        return studioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Studio not found with id: " + id));
    }

    @Override
    public Studio getStudioByUserId(Long userId) {
        return studioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Studio not found for user id: " + userId));
    }

    @Override
    public StudioSummaryDto getStudioSummary(Long id) {
        Studio studio = getStudioById(id);
        return mapToSummary(studio);
    }

    @Override
    public List<StudioSummaryDto> getAllStudios() {
        return studioRepository.findAll().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
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
