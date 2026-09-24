package com.studiolynk.model.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDto {
    private Long id;
    private Long freelancerId;
    private String freelancerName;
    private List<PortfolioCategoryDto> categories = new ArrayList<>();
    private int totalCategories;
    private int totalImages;
    private Instant createdAt;
    private Instant updatedAt;

    public PortfolioDto() {
    }

    public PortfolioDto(Long id, Long freelancerId, String freelancerName,
                        List<PortfolioCategoryDto> categories, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.freelancerId = freelancerId;
        this.freelancerName = freelancerName;
        this.categories = categories != null ? categories : new ArrayList<>();
        this.totalCategories = this.categories.size();
        this.totalImages = this.categories.stream().mapToInt(PortfolioCategoryDto::getImageCount).sum();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public List<PortfolioCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<PortfolioCategoryDto> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.totalCategories = this.categories.size();
        this.totalImages = this.categories.stream().mapToInt(PortfolioCategoryDto::getImageCount).sum();
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }

    public int getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(int totalImages) {
        this.totalImages = totalImages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
