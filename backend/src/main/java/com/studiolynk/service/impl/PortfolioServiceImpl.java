package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ForbiddenException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.CreateCategoryRequestDto;
import com.studiolynk.model.dto.PortfolioCategoryDto;
import com.studiolynk.model.dto.PortfolioDto;
import com.studiolynk.model.dto.PortfolioImageDto;
import com.studiolynk.model.dto.UpdateCategoryRequestDto;
import com.studiolynk.model.dto.UploadResponseDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.Portfolio;
import com.studiolynk.model.entity.PortfolioCategory;
import com.studiolynk.model.entity.PortfolioImage;
import com.studiolynk.model.entity.Studio;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.PortfolioCategoryRepository;
import com.studiolynk.repository.PortfolioImageRepository;
import com.studiolynk.repository.PortfolioRepository;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.PortfolioService;
import com.studiolynk.service.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final PortfolioRepository portfolioRepository;
    private final PortfolioCategoryRepository categoryRepository;
    private final PortfolioImageRepository imageRepository;
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;
    private final StudioRepository studioRepository;
    private final StorageService storageService;

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                PortfolioCategoryRepository categoryRepository,
                                PortfolioImageRepository imageRepository,
                                FreelancerRepository freelancerRepository,
                                UserRepository userRepository,
                                StudioRepository studioRepository,
                                StorageService storageService) {
        this.portfolioRepository = portfolioRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.freelancerRepository = freelancerRepository;
        this.userRepository = userRepository;
        this.studioRepository = studioRepository;
        this.storageService = storageService;
    }

    @Override
    public PortfolioDto getOrCreatePortfolio(String userEmail) {
        User user = getUserByEmail(userEmail);
        if (user.getRole() != UserRole.FREELANCER) {
            throw new BadRequestException("Only Freelancers have portfolios.");
        }

        Freelancer freelancer = freelancerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userEmail));

        Portfolio portfolio = portfolioRepository.findByFreelancerId(freelancer.getId())
                .orElseGet(() -> {
                    Portfolio newPortfolio = new Portfolio(freelancer);
                    return portfolioRepository.save(newPortfolio);
                });

        return mapToPortfolioDto(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioDto getPortfolioByFreelancerId(Long freelancerId) {
        Freelancer freelancer = freelancerRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + freelancerId));

        Portfolio portfolio = portfolioRepository.findByFreelancerId(freelancer.getId())
                .orElseGet(() -> new Portfolio(freelancer));

        return mapToPortfolioDto(portfolio);
    }

    @Override
    public PortfolioCategoryDto createCategory(String userEmail, CreateCategoryRequestDto dto) {
        Portfolio portfolio = getPortfolioEntityForUser(userEmail);

        int sortOrder = dto.getSortOrder() != null ? dto.getSortOrder() : portfolio.getCategories().size();
        PortfolioCategory category = new PortfolioCategory(portfolio, dto.getName().trim(), sortOrder);
        PortfolioCategory saved = categoryRepository.save(category);

        return mapToCategoryDto(saved);
    }

    @Override
    public PortfolioCategoryDto updateCategory(String userEmail, Long categoryId, UpdateCategoryRequestDto dto) {
        PortfolioCategory category = getOwnedCategory(userEmail, categoryId);

        category.setName(dto.getName().trim());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }

        PortfolioCategory saved = categoryRepository.save(category);
        return mapToCategoryDto(saved);
    }

    @Override
    public void deleteCategory(String userEmail, Long categoryId) {
        PortfolioCategory category = getOwnedCategory(userEmail, categoryId);

        // Delete all images in S3/storage
        for (PortfolioImage image : category.getImages()) {
            storageService.deleteFile(image.getS3Key());
        }

        categoryRepository.delete(category);
    }

    @Override
    public List<PortfolioImageDto> uploadImages(String userEmail, Long categoryId, List<MultipartFile> files) {
        PortfolioCategory category = getOwnedCategory(userEmail, categoryId);
        Long freelancerId = category.getPortfolio().getFreelancer().getId();

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("No files provided for upload.");
        }

        int currentSortOrder = category.getImages().stream()
                .mapToInt(PortfolioImage::getSortOrder)
                .max()
                .orElse(-1);

        List<PortfolioImage> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            validateImageFile(file);

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String sanitizedFilename = sanitizeFilename(originalFilename);
            String uniqueFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

            // S3 Key Layout (POR-008, docs/07-PORTFOLIO-S3-SPECIFICATION.md):
            // portfolio/{freelancerId}/{categoryId}/{uniqueFilename}
            String s3Key = String.format("portfolio/%d/%d/%s", freelancerId, categoryId, uniqueFilename);

            try {
                String imageUrl = storageService.uploadFile(
                        s3Key,
                        file.getInputStream(),
                        file.getSize(),
                        file.getContentType()
                );

                currentSortOrder++;
                PortfolioImage image = new PortfolioImage(
                        category,
                        s3Key,
                        imageUrl,
                        originalFilename,
                        file.getContentType(),
                        file.getSize(),
                        currentSortOrder
                );

                savedImages.add(imageRepository.save(image));
            } catch (IOException e) {
                throw new BadRequestException("Failed to read file contents: " + e.getMessage());
            }
        }

        return savedImages.stream().map(this::mapToImageDto).collect(Collectors.toList());
    }

    @Override
    public void deleteImage(String userEmail, Long imageId) {
        PortfolioImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio image not found with id: " + imageId));

        // Ownership check
        String ownerEmail = image.getCategory().getPortfolio().getFreelancer().getUser().getEmail();
        if (!ownerEmail.equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException("You do not have permission to delete this image.");
        }

        storageService.deleteFile(image.getS3Key());
        imageRepository.delete(image);
    }

    @Override
    public List<PortfolioImageDto> reorderImages(String userEmail, Long categoryId, List<Long> imageIds) {
        PortfolioCategory category = getOwnedCategory(userEmail, categoryId);

        List<PortfolioImage> images = imageRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        for (int i = 0; i < imageIds.size(); i++) {
            Long targetId = imageIds.get(i);
            Optional<PortfolioImage> found = images.stream().filter(img -> img.getId().equals(targetId)).findFirst();
            if (found.isPresent()) {
                found.get().setSortOrder(i);
                imageRepository.save(found.get());
            }
        }

        return imageRepository.findByCategoryIdOrderBySortOrderAsc(categoryId).stream()
                .map(this::mapToImageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioCategoryDto> reorderCategories(String userEmail, List<Long> categoryIds) {
        Portfolio portfolio = getPortfolioEntityForUser(userEmail);
        List<PortfolioCategory> categories = portfolio.getCategories();

        for (int i = 0; i < categoryIds.size(); i++) {
            Long targetId = categoryIds.get(i);
            Optional<PortfolioCategory> found = categories.stream().filter(c -> c.getId().equals(targetId)).findFirst();
            if (found.isPresent()) {
                found.get().setSortOrder(i);
                categoryRepository.save(found.get());
            }
        }

        return categoryRepository.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()).stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public UploadResponseDto uploadProfileImage(String userEmail, MultipartFile file) {
        validateImageFile(file);

        User user = getUserByEmail(userEmail);
        String extension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        String s3Key;
        if (user.getRole() == UserRole.STUDIO) {
            Optional<Studio> studio = studioRepository.findByUserId(user.getId());
            Long studioId = studio.map(Studio::getId).orElse(user.getId());
            s3Key = String.format("studio-profiles/%d/%s", studioId, uniqueFilename);
        } else if (user.getRole() == UserRole.FREELANCER) {
            Optional<Freelancer> freelancer = freelancerRepository.findByUserId(user.getId());
            Long freelancerId = freelancer.map(Freelancer::getId).orElse(user.getId());
            s3Key = String.format("freelancer-profiles/%d/%s", freelancerId, uniqueFilename);
        } else {
            s3Key = String.format("profiles/%d/%s", user.getId(), uniqueFilename);
        }

        try {
            String imageUrl = storageService.uploadFile(
                    s3Key,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            return new UploadResponseDto(
                    s3Key,
                    imageUrl,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file: " + e.getMessage());
        }
    }

    // Helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Portfolio getPortfolioEntityForUser(String userEmail) {
        User user = getUserByEmail(userEmail);
        if (user.getRole() != UserRole.FREELANCER) {
            throw new BadRequestException("Only Freelancers manage portfolios.");
        }

        Freelancer freelancer = freelancerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userEmail));

        return portfolioRepository.findByFreelancerId(freelancer.getId())
                .orElseGet(() -> {
                    Portfolio newPortfolio = new Portfolio(freelancer);
                    return portfolioRepository.save(newPortfolio);
                });
    }

    private PortfolioCategory getOwnedCategory(String userEmail, Long categoryId) {
        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio category not found with id: " + categoryId));

        String ownerEmail = category.getPortfolio().getFreelancer().getUser().getEmail();
        if (!ownerEmail.equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException("You do not have access to this portfolio category.");
        }

        return category;
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 10 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file type. Supported formats are: JPEG, PNG, WEBP.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "image";
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private PortfolioDto mapToPortfolioDto(Portfolio portfolio) {
        List<PortfolioCategoryDto> categoryDtos = categoryRepository
                .findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()).stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());

        String freelancerName = portfolio.getFreelancer() != null ? portfolio.getFreelancer().getFullName() : null;
        return new PortfolioDto(
                portfolio.getId(),
                portfolio.getFreelancer() != null ? portfolio.getFreelancer().getId() : null,
                freelancerName,
                categoryDtos,
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    private PortfolioCategoryDto mapToCategoryDto(PortfolioCategory category) {
        List<PortfolioImageDto> imageDtos = imageRepository
                .findByCategoryIdOrderBySortOrderAsc(category.getId()).stream()
                .map(this::mapToImageDto)
                .collect(Collectors.toList());

        return new PortfolioCategoryDto(
                category.getId(),
                category.getPortfolio() != null ? category.getPortfolio().getId() : null,
                category.getName(),
                category.getSortOrder(),
                category.getCreatedAt(),
                imageDtos
        );
    }

    private PortfolioImageDto mapToImageDto(PortfolioImage image) {
        return new PortfolioImageDto(
                image.getId(),
                image.getCategory() != null ? image.getCategory().getId() : null,
                image.getS3Key(),
                image.getImageUrl(),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getFileSize(),
                image.getSortOrder(),
                image.getCreatedAt()
        );
    }
}
