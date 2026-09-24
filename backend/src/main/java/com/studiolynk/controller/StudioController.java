package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.StudioSummaryDto;
import com.studiolynk.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
@Tag(name = "Studio", description = "Studio profile and discovery endpoints")
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

    @GetMapping("/{id}")
    @Operation(summary = "Get studio by ID")
    public ResponseEntity<ApiResponse<StudioSummaryDto>> getStudioById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studioService.getStudioSummary(id)));
    }
}
