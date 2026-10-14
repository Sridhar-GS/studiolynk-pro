package com.studiolynk.model.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for Freelancer directory item in Admin panel (ADM-001, ADM-003).
 */
public class AdminFreelancerItemDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer experienceYears;
    private BigDecimal fullDayRate;
    private BigDecimal halfDayRate;
    private String bio;
    private String profilePhotoUrl;
    private List<String> skills;
    private List<String> services;
    private List<String> equipment;
    private boolean onboardingCompleted;
    private Instant createdAt;
    private long totalRequests;
    private long completedShoots;
    private Double averageRating;
    private Long ratingCount;

    public AdminFreelancerItemDto() {
    }

    public AdminFreelancerItemDto(Long id, Long userId, String fullName, String email,
                                  String phone, String address, BigDecimal latitude,
                                  BigDecimal longitude, Integer experienceYears,
                                  BigDecimal fullDayRate, BigDecimal halfDayRate,
                                  String bio, String profilePhotoUrl, List<String> skills,
                                  List<String> services, List<String> equipment,
                                  boolean onboardingCompleted, Instant createdAt,
                                  long totalRequests, long completedShoots,
                                  Double averageRating, Long ratingCount) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.experienceYears = experienceYears;
        this.fullDayRate = fullDayRate;
        this.halfDayRate = halfDayRate;
        this.bio = bio;
        this.profilePhotoUrl = profilePhotoUrl;
        this.skills = skills;
        this.services = services;
        this.equipment = equipment;
        this.onboardingCompleted = onboardingCompleted;
        this.createdAt = createdAt;
        this.totalRequests = totalRequests;
        this.completedShoots = completedShoots;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getFullDayRate() {
        return fullDayRate;
    }

    public void setFullDayRate(BigDecimal fullDayRate) {
        this.fullDayRate = fullDayRate;
    }

    public BigDecimal getHalfDayRate() {
        return halfDayRate;
    }

    public void setHalfDayRate(BigDecimal halfDayRate) {
        this.halfDayRate = halfDayRate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
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

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getCompletedShoots() {
        return completedShoots;
    }

    public void setCompletedShoots(long completedShoots) {
        this.completedShoots = completedShoots;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }
}
