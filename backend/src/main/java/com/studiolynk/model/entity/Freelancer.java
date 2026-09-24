package com.studiolynk.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Freelancer entity representing photographers, videographers, and creators.
 */
@Entity
@Table(name = "freelancers")
public class Freelancer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "profile_photo_url", length = 512)
    private String profilePhotoUrl;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears = 0;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "full_day_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal fullDayRate = BigDecimal.ZERO;

    @Column(name = "half_day_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal halfDayRate = BigDecimal.ZERO;

    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(
        name = "freelancer_skills",
        joinColumns = @JoinColumn(name = "freelancer_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private java.util.Set<Skill> skills = new java.util.HashSet<>();

    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(
        name = "freelancer_services",
        joinColumns = @JoinColumn(name = "freelancer_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private java.util.Set<ServiceEntity> services = new java.util.HashSet<>();

    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(
        name = "freelancer_equipment",
        joinColumns = @JoinColumn(name = "freelancer_id"),
        inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private java.util.Set<Equipment> equipment = new java.util.HashSet<>();

    public Freelancer() {
    }

    public Freelancer(User user, String fullName, String phone, String address) {
        this.user = user;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public java.util.Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(java.util.Set<Skill> skills) {
        this.skills = skills;
    }

    public java.util.Set<ServiceEntity> getServices() {
        return services;
    }

    public void setServices(java.util.Set<ServiceEntity> services) {
        this.services = services;
    }

    public java.util.Set<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(java.util.Set<Equipment> equipment) {
        this.equipment = equipment;
    }
}
