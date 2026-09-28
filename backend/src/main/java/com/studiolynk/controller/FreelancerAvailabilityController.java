package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.AvailabilityCheckRequestDto;
import com.studiolynk.model.dto.AvailabilityCheckResponseDto;
import com.studiolynk.model.dto.AvailabilityWindowResponseDto;
import com.studiolynk.model.dto.UpdateAvailabilityRequestDto;
import com.studiolynk.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/freelancers")
@Tag(name = "Availability", description = "Rolling 10-day availability calendar and shoot filtering endpoints")
public class FreelancerAvailabilityController {

    private final AvailabilityService availabilityService;

    public FreelancerAvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/me/availability")
    @Operation(summary = "Get current authenticated freelancer's rolling 10-day availability (AVL-001)")
    public ResponseEntity<ApiResponse<AvailabilityWindowResponseDto>> getMyAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {
        AvailabilityWindowResponseDto window = availabilityService.getMyAvailability(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Freelancer availability retrieved successfully.", window));
    }

    @PutMapping("/me/availability")
    @Operation(summary = "Update rolling 10-day availability slots (AVL-002, AVL-003)")
    public ResponseEntity<ApiResponse<AvailabilityWindowResponseDto>> updateMyAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateAvailabilityRequestDto request) {
        AvailabilityWindowResponseDto window = availabilityService.updateMyAvailability(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Availability updated successfully.", window));
    }

    @PostMapping("/me/availability/reset")
    @Operation(summary = "Reset 10-day rolling availability back to NOT_SET")
    public ResponseEntity<ApiResponse<AvailabilityWindowResponseDto>> resetMyAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {
        AvailabilityWindowResponseDto window = availabilityService.resetMyAvailability(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Availability reset back to NOT_SET successfully.", window));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get freelancer's rolling 10-day availability window by ID (AVL-001 - AVL-006)")
    public ResponseEntity<ApiResponse<AvailabilityWindowResponseDto>> getFreelancerAvailability(
            @PathVariable Long id) {
        AvailabilityWindowResponseDto window = availabilityService.getAvailabilityWindowForFreelancer(id);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer availability window retrieved successfully.", window));
    }

    @PostMapping("/availability/check")
    @Operation(summary = "Check candidate availability for a specific date and time interval (AVL-004 - AVL-006)")
    public ResponseEntity<ApiResponse<AvailabilityCheckResponseDto>> checkAvailability(
            @Valid @RequestBody AvailabilityCheckRequestDto request) {
        AvailabilityCheckResponseDto check = availabilityService.checkAvailability(request);
        return ResponseEntity.ok(ApiResponse.ok("Availability check completed.", check));
    }

    @GetMapping("/availability/check")
    @Operation(summary = "Check candidate availability via query parameters (AVL-004 - AVL-006)")
    public ResponseEntity<ApiResponse<AvailabilityCheckResponseDto>> checkAvailabilityQuery(
            @RequestParam Long freelancerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        AvailabilityCheckResponseDto check = availabilityService.checkAvailability(freelancerId, date, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.ok("Availability check completed.", check));
    }
}
