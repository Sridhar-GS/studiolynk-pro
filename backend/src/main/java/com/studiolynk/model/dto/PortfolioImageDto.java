package com.studiolynk.model.dto;

import java.time.Instant;

public class PortfolioImageDto {
    private Long id;
    private Long categoryId;
    private String s3Key;
    private String imageUrl;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private int sortOrder;
    private Instant createdAt;

    public PortfolioImageDto() {
    }

    public PortfolioImageDto(Long id, Long categoryId, String s3Key, String imageUrl,
                             String originalFilename, String contentType, Long fileSize,
                             int sortOrder, Instant createdAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.s3Key = s3Key;
        this.imageUrl = imageUrl;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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
}
