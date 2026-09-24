package com.studiolynk.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Payload for updating Studio profile (STU-005).
 */
public class StudioUpdateRequestDto {

    @NotBlank(message = "Studio name is required")
    @Size(max = 150, message = "Studio name must not exceed 150 characters")
    private String studioName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 150, message = "Owner name must not exceed 150 characters")
    private String ownerName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phone;

    @NotBlank(message = "Studio address is required")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer yearsOfOperation;
    private String logoUrl;

    private List<StudioSocialLinkDto> socialLinks = new ArrayList<>();

    public StudioUpdateRequestDto() {
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

    public List<StudioSocialLinkDto> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<StudioSocialLinkDto> socialLinks) {
        this.socialLinks = socialLinks;
    }
}
