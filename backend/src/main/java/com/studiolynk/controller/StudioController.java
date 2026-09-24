package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.StudioProfileDto;
import com.studiolynk.model.dto.StudioSummaryDto;
import com.studiolynk.model.dto.StudioUpdateRequestDto;
import com.studiolynk.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
@Tag(name = "Studio", description = "Studio profile management and discovery endpoints")
public class StudioController {

    private final StudioService studioService;

    public StudioController(StudioService studioService) {
        this.studioService = studioService;
    }

    @GetMapping
    @Operation(summary = "Get all registered studios")
    public ResponseEntity<ApiResponse<List<StudioSummaryDto>>> getAllStudios() {
        return ResponseEntity.ok(ApiResponse.ok(studioService.getAllStudios()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated studio profile and completion metrics")
    public ResponseEntity<ApiResponse<StudioProfileDto>> getCurrentStudioProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        StudioProfileDto profile = studioService.getStudioProfileByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current studio profile details (STU-005)")
    public ResponseEntity<ApiResponse<StudioProfileDto>> updateCurrentStudioProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudioUpdateRequestDto request) {
        StudioProfileDto updated = studioService.updateStudioProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Studio profile updated successfully.", updated));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get studio profile by ID")
    public ResponseEntity<ApiResponse<StudioProfileDto>> getStudioById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studioService.getStudioProfileById(id)));
    }
}
