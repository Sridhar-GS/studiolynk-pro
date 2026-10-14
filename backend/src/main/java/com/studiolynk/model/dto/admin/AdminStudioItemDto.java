package com.studiolynk.model.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for Studio directory item in Admin panel (ADM-001, ADM-003, ADM-005).
 */
public class AdminStudioItemDto {

    private Long id;
    private Long userId;
    private String studioName;
    private String ownerName;
    private String email;
    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer yearsOfOperation;
    private String logoUrl;
    private boolean onboardingCompleted;
    private Instant createdAt;
    private long totalRequirements;
    private long completedShoots;
    private String identityStatus;
    private Instant identitySubmittedAt;

    public AdminStudioItemDto() {
    }

    public AdminStudioItemDto(Long id, Long userId, String studioName, String ownerName,
                              String email, String phone, String address, BigDecimal latitude,
                              BigDecimal longitude, Integer yearsOfOperation, String logoUrl,
                              boolean onboardingCompleted, Instant createdAt, long totalRequirements,
                              long completedShoots, String identityStatus, Instant identitySubmittedAt) {
        this.id = id;
        this.userId = userId;
        this.studioName = studioName;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.yearsOfOperation = yearsOfOperation;
        this.logoUrl = logoUrl;
        this.onboardingCompleted = onboardingCompleted;
        this.createdAt = createdAt;
        this.totalRequirements = totalRequirements;
        this.completedShoots = completedShoots;
        this.identityStatus = identityStatus;
        this.identitySubmittedAt = identitySubmittedAt;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalRequirements() {
        return totalRequirements;
    }

    public void setTotalRequirements(long totalRequirements) {
        this.totalRequirements = totalRequirements;
    }

    public long getCompletedShoots() {
        return completedShoots;
    }

    public void setCompletedShoots(long completedShoots) {
        this.completedShoots = completedShoots;
    }

    public String getIdentityStatus() {
        return identityStatus;
    }

    public void setIdentityStatus(String identityStatus) {
        this.identityStatus = identityStatus;
    }

    public Instant getIdentitySubmittedAt() {
        return identitySubmittedAt;
    }

    public void setIdentitySubmittedAt(Instant identitySubmittedAt) {
        this.identitySubmittedAt = identitySubmittedAt;
    }
}
