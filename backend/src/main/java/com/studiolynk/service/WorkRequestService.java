package com.studiolynk.service;

import com.studiolynk.model.dto.AcceptRequestDto;
import com.studiolynk.model.dto.CancelRequestDto;
import com.studiolynk.model.dto.CreateWorkRequestDto;
import com.studiolynk.model.dto.WorkRequestResponseDto;
import com.studiolynk.model.dto.WorkRequestSummaryDto;
import com.studiolynk.model.enums.RequestStatus;

import java.util.List;

public interface WorkRequestService {

    // Studio sends work request to a freelancer (REQ-001, WRK-006)
    WorkRequestResponseDto createRequest(String studioEmail, CreateWorkRequestDto dto);

    // Studio reviews sent requests (can filter by requirement or status)
    List<WorkRequestSummaryDto> getStudioRequests(String studioEmail, Long requirementId, RequestStatus status);

    // Freelancer reviews incoming work requests (REQ-002: private client contact masked)
    List<WorkRequestSummaryDto> getFreelancerRequests(String freelancerEmail, RequestStatus status);

    // View request details (REQ-002, REQ-006: dynamic privacy gating for client contact)
    WorkRequestResponseDto getRequestById(String userEmail, Long requestId);

    // Freelancer accepts request (REQ-004)
    WorkRequestResponseDto acceptRequest(String freelancerEmail, Long requestId, AcceptRequestDto dto);

    // Freelancer rejects request (REQ-004)
    WorkRequestResponseDto rejectRequest(String freelancerEmail, Long requestId, String reason);

    // Studio confirms the final freelancer (REQ-005, WRK-007, WRK-008: closes other pending requests)
    WorkRequestResponseDto confirmRequest(String studioEmail, Long requestId);

    // Either party cancels confirmed work with a reason (REQ-007)
    WorkRequestResponseDto cancelRequest(String userEmail, Long requestId, CancelRequestDto dto);
}
