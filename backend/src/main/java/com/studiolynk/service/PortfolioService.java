package com.studiolynk.service;

import com.studiolynk.model.dto.CreateCategoryRequestDto;
import com.studiolynk.model.dto.PortfolioCategoryDto;
import com.studiolynk.model.dto.PortfolioDto;
import com.studiolynk.model.dto.PortfolioImageDto;
import com.studiolynk.model.dto.UpdateCategoryRequestDto;
import com.studiolynk.model.dto.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PortfolioService {
    PortfolioDto getOrCreatePortfolio(String userEmail);
    PortfolioDto getPortfolioByFreelancerId(Long freelancerId);

    PortfolioCategoryDto createCategory(String userEmail, CreateCategoryRequestDto dto);
    PortfolioCategoryDto updateCategory(String userEmail, Long categoryId, UpdateCategoryRequestDto dto);
    void deleteCategory(String userEmail, Long categoryId);

    List<PortfolioImageDto> uploadImages(String userEmail, Long categoryId, List<MultipartFile> files);
    void deleteImage(String userEmail, Long imageId);

    List<PortfolioImageDto> reorderImages(String userEmail, Long categoryId, List<Long> imageIds);
    List<PortfolioCategoryDto> reorderCategories(String userEmail, List<Long> categoryIds);

    UploadResponseDto uploadProfileImage(String userEmail, MultipartFile file);
}
