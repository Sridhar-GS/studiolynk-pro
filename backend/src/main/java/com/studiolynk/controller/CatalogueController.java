package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.CustomEquipmentRequestDto;
import com.studiolynk.model.dto.CustomItemRequestDto;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.EquipmentCategory;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.entity.User;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.CatalogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Catalogue", description = "Skills, Services, and Equipment master catalog endpoints")
public class CatalogueController {

    private final CatalogueService catalogueService;
    private final UserRepository userRepository;

    public CatalogueController(CatalogueService catalogueService, UserRepository userRepository) {
        this.catalogueService = catalogueService;
        this.userRepository = userRepository;
    }

    @GetMapping("/skills")
    @Operation(summary = "Get all available skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkills() {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getAllSkills()));
    }

    @PostMapping("/skills/custom")
    @Operation(summary = "Add a custom photography/videography skill (FRL-002)")
    public ResponseEntity<ApiResponse<Skill>> createCustomSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomItemRequestDto request) {
        Long userId = getUserId(userDetails);
        Skill created = catalogueService.createCustomSkill(request.getName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Custom skill created successfully.", created));
    }

    @GetMapping("/services")
    @Operation(summary = "Get all available services")
    public ResponseEntity<ApiResponse<List<ServiceEntity>>> getServices() {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getAllServices()));
    }

    @PostMapping("/services/custom")
    @Operation(summary = "Add a custom photography service (FRL-003)")
    public ResponseEntity<ApiResponse<ServiceEntity>> createCustomService(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomItemRequestDto request) {
        Long userId = getUserId(userDetails);
        ServiceEntity created = catalogueService.createCustomService(request.getName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Custom service created successfully.", created));
    }

    @GetMapping("/equipment/categories")
    @Operation(summary = "Get all equipment categories")
    public ResponseEntity<ApiResponse<List<EquipmentCategory>>> getEquipmentCategories() {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getAllEquipmentCategories()));
    }

    @GetMapping("/equipment")
    @Operation(summary = "Get all equipment items")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment() {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getAllEquipment()));
    }

    @GetMapping("/equipment/category/{categoryId}")
    @Operation(summary = "Get equipment items for a specific category")
    public ResponseEntity<ApiResponse<List<Equipment>>> getEquipmentByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getEquipmentByCategory(categoryId)));
    }

    @PostMapping("/equipment/custom")
    @Operation(summary = "Add custom equipment gear under a category (FRL-005)")
    public ResponseEntity<ApiResponse<Equipment>> createCustomEquipment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomEquipmentRequestDto request) {
        Long userId = getUserId(userDetails);
        Equipment created = catalogueService.createCustomEquipment(request.getCategoryId(), request.getName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Custom equipment gear created successfully.", created));
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails != null) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
}
