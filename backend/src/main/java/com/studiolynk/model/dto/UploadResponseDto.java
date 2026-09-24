package com.studiolynk.model.dto;

public class UploadResponseDto {

    private String s3Key;
    private String imageUrl;
    private String originalFilename;
    private String contentType;
    private Long fileSize;

    public UploadResponseDto() {
    }

    public UploadResponseDto(String s3Key, String imageUrl, String originalFilename, String contentType, Long fileSize) {
        this.s3Key = s3Key;
        this.imageUrl = imageUrl;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
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
}
