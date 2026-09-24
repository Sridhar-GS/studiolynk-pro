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
 * Studio entity representing photography studios on StudioLynk.
 */
@Entity
@Table(name = "studios")
public class Studio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "studio_name", nullable = false, length = 150)
    private String studioName;

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "years_of_operation")
    private Integer yearsOfOperation = 0;

    @jakarta.persistence.OneToMany(mappedBy = "studio", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private java.util.List<StudioSocialLink> socialLinks = new java.util.ArrayList<>();

    @jakarta.persistence.OneToMany(mappedBy = "studio", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private java.util.List<StudioIdentitySubmission> identitySubmissions = new java.util.ArrayList<>();

    public Studio() {
    }

    public Studio(User user, String studioName, String ownerName, String phone, String address) {
        this.user = user;
        this.studioName = studioName;
        this.ownerName = ownerName;
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

    public java.util.List<StudioSocialLink> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(java.util.List<StudioSocialLink> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public java.util.List<StudioIdentitySubmission> getIdentitySubmissions() {
        return identitySubmissions;
    }

    public void setIdentitySubmissions(java.util.List<StudioIdentitySubmission> identitySubmissions) {
        this.identitySubmissions = identitySubmissions;
    }
}
