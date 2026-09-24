package com.studiolynk.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Full Studio profile representation including social links, identity declaration, and completion metric.
 */
public class StudioProfileDto {
    private Long id;
    private Long userId;
    private String email;
    private String studioName;
    private String ownerName;
    private String logoUrl;
    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer yearsOfOperation;
    private boolean onboardingCompleted;
    private int completionPercentage;
    private List<StudioSocialLinkDto> socialLinks = new ArrayList<>();
    private StudioIdentitySubmissionDto identitySubmission;
    private Instant createdAt;
    private Instant updatedAt;

    public StudioProfileDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudioName() {
        return studioName;
    }

    public void setStudioName(String studioName) {
        this.studioName = studioName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getYearsOfOperation() {
        return yearsOfOperation;
    }

    public void setYearsOfOperation(Integer yearsOfOperation) {
        this.yearsOfOperation = yearsOfOperation;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public List<StudioSocialLinkDto> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<StudioSocialLinkDto> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public StudioIdentitySubmissionDto getIdentitySubmission() {
        return identitySubmission;
    }

    public void setIdentitySubmission(StudioIdentitySubmissionDto identitySubmission) {
        this.identitySubmission = identitySubmission;
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
