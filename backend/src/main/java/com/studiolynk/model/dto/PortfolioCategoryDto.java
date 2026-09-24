package com.studiolynk.model.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PortfolioCategoryDto {
    private Long id;
    private Long portfolioId;
    private String name;
    private int sortOrder;
    private Instant createdAt;
    private List<PortfolioImageDto> images = new ArrayList<>();
    private int imageCount;

    public PortfolioCategoryDto() {
    }

    public PortfolioCategoryDto(Long id, Long portfolioId, String name, int sortOrder,
                                Instant createdAt, List<PortfolioImageDto> images) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.name = name;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.images = images != null ? images : new ArrayList<>();
        this.imageCount = this.images.size();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<PortfolioImageDto> getImages() {
        return images;
    }

    public void setImages(List<PortfolioImageDto> images) {
        this.images = images != null ? images : new ArrayList<>();
        this.imageCount = this.images.size();
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
}
