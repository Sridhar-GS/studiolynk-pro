package com.studiolynk.model.dto;

import jakarta.validation.constraints.NotBlank;

public class StudioSocialLinkDto {
    private Long id;

    @NotBlank(message = "Platform name is required")
    private String platformName;

    @NotBlank(message = "URL is required")
    private String url;

    public StudioSocialLinkDto() {
    }

    public StudioSocialLinkDto(Long id, String platformName, String url) {
        this.id = id;
        this.platformName = platformName;
        this.url = url;
    }

    public StudioSocialLinkDto(String platformName, String url) {
        this.platformName = platformName;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
