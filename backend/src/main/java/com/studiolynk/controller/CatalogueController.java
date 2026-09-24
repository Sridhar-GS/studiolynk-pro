package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.EquipmentCategory;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.service.CatalogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Catalogue", description = "Skills, Services, and Equipment master catalog endpoints")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/skills")
    @Operation(summary = "Get all available skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkills() {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getAllSkills()));
    }

    @GetMapping("/services")
    @Operation(summary = "Get all available services")
    public ResponseEntity<ApiResponse<List<ServiceEntity>>> getServices() {
        return ResponseEntity.ok(ApiResponse.ok(catalogueService.getAllServices()));
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
}
