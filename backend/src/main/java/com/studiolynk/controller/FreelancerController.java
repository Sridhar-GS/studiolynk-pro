package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.FreelancerSummaryDto;
import com.studiolynk.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/freelancers")
@Tag(name = "Freelancer", description = "Freelancer profile and search endpoints")
public class FreelancerController {

    private final FreelancerService freelancerService;

    public FreelancerController(FreelancerService freelancerService) {
        this.freelancerService = freelancerService;
    }

    @GetMapping
    @Operation(summary = "Get all registered freelancers")
    public ResponseEntity<ApiResponse<List<FreelancerSummaryDto>>> getAllFreelancers() {
        return ResponseEntity.ok(ApiResponse.ok(freelancerService.getAllFreelancers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get freelancer by ID")
    public ResponseEntity<ApiResponse<FreelancerSummaryDto>> getFreelancerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(freelancerService.getFreelancerSummary(id)));
    }
}
