package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.CreateCategoryRequestDto;
import com.studiolynk.model.dto.PortfolioCategoryDto;
import com.studiolynk.model.dto.PortfolioDto;
import com.studiolynk.model.dto.PortfolioImageDto;
import com.studiolynk.model.dto.ReorderItemsRequestDto;
import com.studiolynk.model.dto.UpdateCategoryRequestDto;
import com.studiolynk.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Portfolio", description = "Freelancer portfolio, categories, image upload, and reordering (Phase 6)")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated freelancer's portfolio with all categories and images (POR-001)")
    public ResponseEntity<ApiResponse<PortfolioDto>> getMyPortfolio(@AuthenticationPrincipal UserDetails userDetails) {
        PortfolioDto portfolio = portfolioService.getOrCreatePortfolio(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(portfolio));
    }

    @GetMapping("/freelancer/{freelancerId}")
    @Operation(summary = "Get freelancer portfolio by freelancer ID (for studios / public view)")
    public ResponseEntity<ApiResponse<PortfolioDto>> getPortfolioByFreelancerId(@PathVariable Long freelancerId) {
        PortfolioDto portfolio = portfolioService.getPortfolioByFreelancerId(freelancerId);
        return ResponseEntity.ok(ApiResponse.ok(portfolio));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new portfolio category (POR-002, POR-003, POR-005)")
    public ResponseEntity<ApiResponse<PortfolioCategoryDto>> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCategoryRequestDto request) {
        PortfolioCategoryDto created = portfolioService.createCategory(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created successfully.", created));
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Update an existing portfolio category name or sort order (POR-005)")
    public ResponseEntity<ApiResponse<PortfolioCategoryDto>> updateCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequestDto request) {
        PortfolioCategoryDto updated = portfolioService.updateCategory(userDetails.getUsername(), categoryId, request);
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully.", updated));
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "Delete a portfolio category and all its stored images (POR-005)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId) {
        portfolioService.deleteCategory(userDetails.getUsername(), categoryId);
        return ResponseEntity.ok(ApiResponse.ok("Category and its images deleted successfully.", null));
    }

    @PostMapping(value = "/categories/{categoryId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image(s) to a portfolio category (POR-004, POR-008)")
    public ResponseEntity<ApiResponse<List<PortfolioImageDto>>> uploadImages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId,
            @RequestParam("files") List<MultipartFile> files) {
        List<PortfolioImageDto> uploaded = portfolioService.uploadImages(userDetails.getUsername(), categoryId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Image(s) uploaded successfully.", uploaded));
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Delete a portfolio image from storage and database (POR-005)")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long imageId) {
        portfolioService.deleteImage(userDetails.getUsername(), imageId);
        return ResponseEntity.ok(ApiResponse.ok("Image deleted successfully.", null));
    }

    @PutMapping("/categories/{categoryId}/reorder")
    @Operation(summary = "Reorder images within a category via drag-and-drop ID sequence (POR-006)")
    public ResponseEntity<ApiResponse<List<PortfolioImageDto>>> reorderImages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId,
            @Valid @RequestBody ReorderItemsRequestDto request) {
        List<PortfolioImageDto> reordered = portfolioService.reorderImages(userDetails.getUsername(), categoryId, request.getItemIds());
        return ResponseEntity.ok(ApiResponse.ok("Images reordered successfully.", reordered));
    }

    @PutMapping("/categories/reorder")
    @Operation(summary = "Reorder categories within a portfolio")
    public ResponseEntity<ApiResponse<List<PortfolioCategoryDto>>> reorderCategories(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReorderItemsRequestDto request) {
        List<PortfolioCategoryDto> reordered = portfolioService.reorderCategories(userDetails.getUsername(), request.getItemIds());
        return ResponseEntity.ok(ApiResponse.ok("Categories reordered successfully.", reordered));
    }
}
