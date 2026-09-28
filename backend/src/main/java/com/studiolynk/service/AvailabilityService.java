package com.studiolynk.service;

import com.studiolynk.model.dto.AvailabilityCheckRequestDto;
import com.studiolynk.model.dto.AvailabilityCheckResponseDto;
import com.studiolynk.model.dto.AvailabilityWindowResponseDto;
import com.studiolynk.model.dto.UpdateAvailabilityRequestDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityService {

    // Freelancer managing their rolling 10-day window (AVL-001 - AVL-003)
    AvailabilityWindowResponseDto getMyAvailability(String userEmail);

    AvailabilityWindowResponseDto updateMyAvailability(String userEmail, UpdateAvailabilityRequestDto request);

    AvailabilityWindowResponseDto resetMyAvailability(String userEmail);

    // Studio or viewer inspecting a freelancer's availability (AVL-001 - AVL-006)
    AvailabilityWindowResponseDto getAvailabilityWindowForFreelancer(Long freelancerId);

    // Studio checking a specific shoot date/time against candidate (AVL-004, AVL-005, AVL-006)
    AvailabilityCheckResponseDto checkAvailability(Long freelancerId, LocalDate date, LocalTime startTime, LocalTime endTime);

    AvailabilityCheckResponseDto checkAvailability(AvailabilityCheckRequestDto request);

    // Hard filtering candidate freelancer IDs for discovery & ML (AVL-004, AVL-007)
    List<Long> filterAvailableFreelancerIds(List<Long> freelancerIds, LocalDate date, LocalTime startTime, LocalTime endTime);

    // Conflict detection for booking confirmation (AVL-008, AVL-009)
    boolean hasConflict(Long freelancerId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
