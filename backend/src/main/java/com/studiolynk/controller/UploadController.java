package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.UploadResponseDto;
import com.studiolynk.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "Image upload endpoints for profile photos and studio logos (Phase 6)")
public class UploadController {

    private final PortfolioService portfolioService;

    public UploadController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo or studio logo to S3/storage (Phase 6)")
    public ResponseEntity<ApiResponse<UploadResponseDto>> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UploadResponseDto response = portfolioService.uploadProfileImage(userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Image uploaded successfully.", response));
    }
}
