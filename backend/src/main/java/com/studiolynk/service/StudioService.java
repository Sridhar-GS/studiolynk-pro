package com.studiolynk.service;

import com.studiolynk.model.dto.StudioSummaryDto;
import com.studiolynk.model.entity.Studio;

import java.util.List;

public interface StudioService {
    Studio getStudioById(Long id);
    Studio getStudioByUserId(Long userId);
    StudioSummaryDto getStudioSummary(Long id);
    List<StudioSummaryDto> getAllStudios();
}
