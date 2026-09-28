package com.studiolynk.model.dto;

import com.studiolynk.model.enums.AvailabilityStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FreelancerCardDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String profilePhotoUrl;
    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double distanceKm;
    private String formattedDistance;
    private Integer experienceYears;
    private String bio;
    private BigDecimal fullDayRate;
    private BigDecimal halfDayRate;
    private Double averageRating;
    private Integer reviewCount;
    private List<SkillDto> skills = new ArrayList<>();
    private List<ServiceDto> services = new ArrayList<>();
    private List<EquipmentDto> equipment = new ArrayList<>();
    private String primaryRole;
    private AvailabilityStatus availabilityStatus;
    private String availableHours;
    private Boolean withinWindow;
    private String availabilityNotice;
    private Double aiMatchScore;
    private boolean onboardingCompleted;

    public FreelancerCardDto() {
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

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getFormattedDistance() {
        return formattedDistance;
    }

    public void setFormattedDistance(String formattedDistance) {
        this.formattedDistance = formattedDistance;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<SkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillDto> skills) {
        this.skills = skills;
    }

    public List<ServiceDto> getServices() {
        return services;
    }

    public void setServices(List<ServiceDto> services) {
        this.services = services;
    }

    public List<EquipmentDto> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<EquipmentDto> equipment) {
        this.equipment = equipment;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(String availableHours) {
        this.availableHours = availableHours;
    }

    public Boolean isWithinWindow() {
        return withinWindow;
    }

    public Boolean getWithinWindow() {
        return withinWindow;
    }

    public void setWithinWindow(Boolean withinWindow) {
        this.withinWindow = withinWindow;
    }

    public String getAvailabilityNotice() {
        return availabilityNotice;
    }

    public void setAvailabilityNotice(String availabilityNotice) {
        this.availabilityNotice = availabilityNotice;
    }

    public Double getAiMatchScore() {
        return aiMatchScore;
    }

    public void setAiMatchScore(Double aiMatchScore) {
        this.aiMatchScore = aiMatchScore;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }
}
