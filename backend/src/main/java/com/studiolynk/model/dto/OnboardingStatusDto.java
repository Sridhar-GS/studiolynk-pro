package com.studiolynk.model.dto;

import com.studiolynk.model.enums.UserRole;

public class OnboardingStatusDto {
    private Long userId;
    private String email;
    private UserRole role;
    private boolean onboardingCompleted;
    private String redirectUrl;

    public OnboardingStatusDto() {
    }

    public OnboardingStatusDto(Long userId, String email, UserRole role, boolean onboardingCompleted, String redirectUrl) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.onboardingCompleted = onboardingCompleted;
        this.redirectUrl = redirectUrl;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
